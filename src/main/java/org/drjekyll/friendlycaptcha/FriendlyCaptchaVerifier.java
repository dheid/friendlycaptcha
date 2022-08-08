package org.drjekyll.friendlycaptcha;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;

/**
 * Verifies a Friendly Captcha Solution by making an HTTP POST request to the Friendly Captcha API
 * endpoint.
 *
 * <p>You will need an API key (secret) that you can create on the <a
 * href="https://friendlycaptcha.com/account">Friendly Captcha account page</a></p>
 */
@Slf4j
@Builder
public class FriendlyCaptchaVerifier {

  /**
   * An API key that proves it's you, create one on the Friendly Captcha website
   */
  @NonNull
  private final String apiKey;

  @NonNull
  @Builder.Default
  private ObjectMapper objectMapper = new ObjectMapper();

  /**
   * The URI that points to the Verification API endpoint
   * <p>
   * Default: https://api.friendlycaptcha.com/api/v1/siteverify
   */
  @NonNull
  @Builder.Default
  private URI verificationEndpoint =
    URI.create("https://api.friendlycaptcha.com/api/v1/siteverify");

  /**
   * The timeout until a connection is established.
   *
   * <p>A timeout value of zero is interpreted as an infinite timeout.
   * A `null` value is interpreted as undefined (system default if applicable).</p>
   *
   * <p>Default: 10 seconds</p>
   */
  @Builder.Default
  private Duration connectTimeout = Duration.ofSeconds(10L);

  /**
   * The socket timeout ({@code SO_TIMEOUT}), which is the timeout for waiting for data or, put
   * differently, a maximum period inactivity between two consecutive data packets).
   *
   * <p>A timeout value of zero is interpreted as an infinite timeout.
   * A `null value is interpreted as undefined (system default if applicable).</p>
   *
   * <p>Default: 30 seconds</p>
   */
  @Builder.Default
  private Duration socketTimeout = Duration.ofSeconds(30L);

  /**
   * An optional sitekey that you want to make sure the puzzle was generated from.
   */
  @Nullable
  private String sitekey;

  /**
   * The hostname or IP address of an optional HTTP proxy. {@code proxyPort} must be configured as
   * well
   */
  @Nullable
  private String proxyHost;

  /**
   * The port of an HTTP proxy. {@code proxyHost} must be configured as well.
   */
  private int proxyPort;

  /**
   * If the HTTP proxy requires a user name for basic authentication, it can be configured here.
   * Proxy host, port and password must also be set.
   */
  @Nullable
  private String proxyUserName;

  /**
   * The corresponding password for the basic auth proxy user. The proxy host, port and user name
   * must be set as well.
   */
  @Nullable
  private String proxyPassword;

  /**
   * Logs INFO messages with detailed information
   */
  private boolean verbose;

  public boolean verify(@Nonnull String solution) {
    assertNotEmpty(solution, "Solution must not be null or empty");
    assertNotEmpty(apiKey, "API key must not be null or empty");
    assertVerificationEndpointScheme();

    byte[] entity = createEntity(solution);
    HttpURLConnection connection = connect(entity.length);

    if (verbose) {
      log.info("Verifying friendly captcha solution using endpoint {}", verificationEndpoint);
    }

    try {
      connection.connect();
    } catch (IOException e) {
      connection.disconnect();
      throw new FriendlyCaptchaException("Could not connect", e);
    }

    writeEntity(entity, connection);

    try (InputStream inputStream = getInputStream(connection)) {
      VerificationResponse verificationResponse = readVerificationResponse(inputStream);
      return processResponse(connection, verificationResponse);
    } catch (IOException e) {
      throw new FriendlyCaptchaException("Could not check solution", e);
    } finally {
      connection.disconnect();
    }

  }

  private boolean processResponse(HttpURLConnection connection, VerificationResponse verificationResponse) throws IOException {
    if (verbose) {
      log.info("Received response {} with status code {}", verificationResponse, connection.getResponseCode());
    }
    if (connection.getResponseCode() == 200) {
      return verificationResponse.isSuccess();
    }

    Collection<VerificationError> errors = verificationResponse.getErrors();

    if (errors == null || errors.isEmpty()) {
      throw new FriendlyCaptchaException("Verification API did not return any error");
    }

    log.warn("Received response with errors from Verification API: {}", verificationResponse);

    VerificationError verificationError = errors.iterator().next();
    String message = isEmpty(verificationResponse.getDetails())
      ? verificationError.getDescription()
      : verificationResponse.getDetails();
    throw new FriendlyCaptchaException(message);
  }

  @Nonnull
  private static InputStream getInputStream(@NonNull HttpURLConnection connection) {
    try {
      if (connection.getResponseCode() == 200) {
        return connection.getInputStream();
      }
      return connection.getErrorStream();
    } catch (IOException exception) {
      connection.disconnect();
      throw new FriendlyCaptchaException("Could not read response", exception);
    }
  }

  private static void writeEntity(@NonNull byte[] entity, @NonNull HttpURLConnection connection) {
    try (OutputStream outputStream = connection.getOutputStream()) {
      outputStream.write(entity);
      outputStream.flush();
    } catch (IOException e) {
      connection.disconnect();
      throw new FriendlyCaptchaException("Could not transfer solution", e);
    }
  }

  private HttpURLConnection connect(int contentLength) {
    HttpURLConnection connection;
    try {
      if (isEmpty(proxyHost) || proxyPort <= 0) {
        connection = (HttpURLConnection) verificationEndpoint.toURL().openConnection();
      } else {
        InetSocketAddress proxyAddress = new InetSocketAddress(proxyHost, proxyPort);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyAddress);
        if (!isEmpty(proxyUserName) && !isEmpty(proxyPassword)) {
          Authenticator.setDefault(new ProxyAuthenticator(proxyUserName, proxyPassword));
        }
        connection = (HttpURLConnection) verificationEndpoint.toURL().openConnection(proxy);
      }
    } catch (IOException e) {
      throw new FriendlyCaptchaException("Could not open connection", e);
    }

    try {
      connection.setRequestMethod("POST");
    } catch (ProtocolException e) {
      throw new FriendlyCaptchaException("Could not set request method", e);
    }
    connection.setDoInput(true);
    connection.setDoOutput(true);
    connection.setUseCaches(false);
    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    connection.setRequestProperty("Accept", "application/json");
    connection.setRequestProperty("User-Agent", "FriendlyCaptchaJavaClient");
    connection.setRequestProperty("charset", "utf-8");
    connection.setRequestProperty("Content-Length", Integer.toString(contentLength));
    connection.setRequestProperty("Connection", "close");

    if (connectTimeout != null) {
      connection.setConnectTimeout((int) connectTimeout.toMillis());
    }
    if (socketTimeout != null) {
      connection.setReadTimeout((int) socketTimeout.toMillis());
    }
    return connection;
  }

  private static void assertNotEmpty(@Nonnull String str, @Nullable String message) {
    if (isEmpty(str)) {
      throw new IllegalArgumentException(message);
    }
  }

  private void assertVerificationEndpointScheme() {
    String scheme = verificationEndpoint.getScheme();
    if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
      throw new FriendlyCaptchaException("Invalid verification endpoint URL");
    }
  }

  @Nonnull
  private byte[] createEntity(@Nonnull String solution) {
    assertNotEmpty(solution, "Solution must not be null or empty");
    try {
      String entity = "solution=" + URLEncoder.encode(solution, "UTF-8") + "&secret=" + URLEncoder.encode(apiKey, "UTF-8");
      if (!isEmpty(sitekey)) {
        entity += "&sitekey=" + sitekey;
      }
      return entity.getBytes(StandardCharsets.UTF_8);
    } catch (UnsupportedEncodingException e) {
      throw new FriendlyCaptchaException("Could not encode payload", e);
    }
  }

  private VerificationResponse readVerificationResponse(@Nonnull InputStream inputStream) {
    try {
      return objectMapper.readValue(inputStream, VerificationResponse.class);
    } catch (Exception e) {
      throw new FriendlyCaptchaException("Could not read response from verification API", e);
    }
  }

  private static boolean isEmpty(@Nullable String str) {
    return str == null || str.trim().isEmpty();
  }

}
