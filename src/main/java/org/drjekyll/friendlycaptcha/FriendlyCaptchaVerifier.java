package org.drjekyll.friendlycaptcha;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URI;
import java.time.Duration;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base class for verifying Friendly Captcha solutions. Use {@link
 * FriendlyCaptchaVerifierV1} for the v1 API or {@link FriendlyCaptchaVerifierV2} for the v2 API.
 *
 * <p>You will need an API key that you can create on the <a
 * href="https://friendlycaptcha.com/account">Friendly Captcha account page</a>.
 */
@Slf4j
@SuperBuilder
public abstract class FriendlyCaptchaVerifier {

  /** An API key that proves it's you, create one on the Friendly Captcha website. */
  @NonNull protected final String apiKey;

  @NonNull @lombok.Builder.Default private ObjectMapper objectMapper = new ObjectMapper();

  /**
   * The URI that points to the verification API endpoint.
   *
   * <p>If not set, each version uses its own default endpoint.
   */
  @Nullable private URI verificationEndpoint;

  /**
   * The timeout until a connection is established.
   *
   * <p>A timeout value of zero is interpreted as an infinite timeout. A {@code null} value is
   * interpreted as undefined (system default if applicable).
   *
   * <p>Default: 10 seconds
   */
  @lombok.Builder.Default private Duration connectTimeout = Duration.ofSeconds(10L);

  /**
   * The socket timeout ({@code SO_TIMEOUT}), which is the timeout for waiting for data or, put
   * differently, a maximum period of inactivity between two consecutive data packets.
   *
   * <p>A timeout value of zero is interpreted as an infinite timeout. A {@code null} value is
   * interpreted as undefined (system default if applicable).
   *
   * <p>Default: 30 seconds
   */
  @lombok.Builder.Default private Duration socketTimeout = Duration.ofSeconds(30L);

  /** An optional sitekey that you want to make sure the puzzle was generated from. */
  @Nullable protected String sitekey;

  /**
   * The hostname or IP address of an optional HTTP proxy. {@code proxyPort} must be configured as
   * well.
   */
  @Nullable private String proxyHost;

  /** The port of an HTTP proxy. {@code proxyHost} must be configured as well. */
  private int proxyPort;

  /**
   * If the HTTP proxy requires a user name for basic authentication, it can be configured here.
   * Proxy host, port and password must also be set.
   */
  @Nullable private String proxyUserName;

  /**
   * The corresponding password for the basic auth proxy user. The proxy host, port and user name
   * must be set as well.
   */
  @Nullable private String proxyPassword;

  /** Logs INFO messages with detailed information. */
  protected boolean verbose;

  /**
   * Creates a builder for the v1 API.
   *
   * @deprecated Use {@link FriendlyCaptchaVerifierV1#builder()} or {@link
   *     FriendlyCaptchaVerifierV2#builder()} instead.
   */
  @Deprecated
  public static FriendlyCaptchaVerifierBuilder<?, ?> builder() {
    return FriendlyCaptchaVerifierV1.builder();
  }

  /**
   * Verifies the given captcha solution against the Friendly Captcha API.
   *
   * @param solution the captcha response value submitted by the user
   * @return {@code true} if the solution is valid, {@code false} if it was rejected by the API
   * @throws IllegalArgumentException if solution or API key is null or empty
   * @throws FriendlyCaptchaException if the API returns an error (authentication failure, bad
   *     request, etc.) or the response cannot be read
   */
  public final boolean verify(@Nonnull String solution) {
    assertNotEmpty(solution, "Solution must not be null or empty");
    assertNotEmpty(apiKey, "API key must not be null or empty");

    URI effectiveEndpoint =
        verificationEndpoint != null ? verificationEndpoint : getDefaultEndpoint();
    assertVerificationEndpointScheme(effectiveEndpoint);

    byte[] entity = buildRequestBody(solution);
    HttpURLConnection connection = connect(entity.length, effectiveEndpoint);

    if (verbose) {
      log.info("Verifying friendly captcha solution using endpoint {}", effectiveEndpoint);
    }

    try {
      connection.connect();
    } catch (IOException e) {
      connection.disconnect();
      throw new FriendlyCaptchaException("Could not connect", e);
    }

    writeEntity(entity, connection);

    try (InputStream inputStream = getInputStream(connection)) {
      return processResponse(connection, inputStream);
    } catch (IOException e) {
      throw new FriendlyCaptchaException("Could not check solution", e);
    } finally {
      connection.disconnect();
    }
  }

  /** Returns the default verification endpoint URL for this API version. */
  protected abstract URI getDefaultEndpoint();

  /** Builds the URL-encoded POST body for the given captcha solution. */
  protected abstract byte[] buildRequestBody(String solution);

  /**
   * Sets any version-specific request headers on the connection (e.g. {@code X-API-Key} for v2).
   */
  protected abstract void configureVersionSpecificHeaders(HttpURLConnection connection);

  /**
   * Parses the response body and returns {@code true} if the solution is valid, {@code false} if
   * rejected, or throws {@link FriendlyCaptchaException} on API errors.
   */
  protected abstract boolean processResponse(HttpURLConnection connection, InputStream inputStream)
      throws IOException;

  /**
   * Reads and deserialises the response body into the given class using the shared ObjectMapper.
   */
  protected <T> T readResponse(@Nonnull InputStream inputStream, @Nonnull Class<T> responseClass) {
    try {
      return objectMapper.readValue(inputStream, responseClass);
    } catch (Exception e) {
      throw new FriendlyCaptchaException("Could not read response from verification API", e);
    }
  }

  protected static boolean isEmpty(@Nullable String str) {
    return str == null || str.trim().isEmpty();
  }

  private HttpURLConnection connect(int contentLength, @Nonnull URI endpoint) {
    HttpURLConnection connection;
    try {
      if (isEmpty(proxyHost) || proxyPort <= 0) {
        connection = (HttpURLConnection) endpoint.toURL().openConnection();
      } else {
        InetSocketAddress proxyAddress = new InetSocketAddress(proxyHost, proxyPort);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyAddress);
        if (!isEmpty(proxyUserName) && !isEmpty(proxyPassword)) {
          Authenticator.setDefault(new ProxyAuthenticator(proxyUserName, proxyPassword));
        }
        connection = (HttpURLConnection) endpoint.toURL().openConnection(proxy);
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
    connection.setRequestProperty("Content-Length", Integer.toString(contentLength));
    connection.setRequestProperty("Connection", "close");

    configureVersionSpecificHeaders(connection);

    if (connectTimeout != null) {
      connection.setConnectTimeout((int) connectTimeout.toMillis());
    }
    if (socketTimeout != null) {
      connection.setReadTimeout((int) socketTimeout.toMillis());
    }
    return connection;
  }

  @Nonnull
  private static InputStream getInputStream(@NonNull HttpURLConnection connection) {
    try {
      if (connection.getResponseCode() == 200) {
        return connection.getInputStream();
      }
      InputStream errorStream = connection.getErrorStream();
      return errorStream != null ? errorStream : new ByteArrayInputStream(new byte[0]);
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

  private static void assertNotEmpty(@Nonnull String str, @Nullable String message) {
    if (isEmpty(str)) {
      throw new IllegalArgumentException(message);
    }
  }

  private static void assertVerificationEndpointScheme(@Nonnull URI endpoint) {
    String scheme = endpoint.getScheme();
    if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
      throw new FriendlyCaptchaException("Invalid verification endpoint URL");
    }
  }
}
