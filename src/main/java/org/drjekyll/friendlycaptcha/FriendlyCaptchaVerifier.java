package org.drjekyll.friendlycaptcha;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
   * The timeout for the entire request (connecting, sending, and receiving the response).
   *
   * <p>A {@code null} value means no request timeout is applied.
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

    if (verbose) {
      log.info("Verifying friendly captcha solution using endpoint {}", effectiveEndpoint);
    }

    byte[] body = buildRequestBody(solution);
    HttpClient client = buildHttpClient();
    HttpRequest request = buildHttpRequest(body, effectiveEndpoint);

    try {
      HttpResponse<InputStream> response =
          client.send(request, HttpResponse.BodyHandlers.ofInputStream());
      return processResponse(response.statusCode(), response.body());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new FriendlyCaptchaException("Could not check solution", e);
    } catch (IOException e) {
      throw new FriendlyCaptchaException("Could not check solution", e);
    }
  }

  /** Returns the default verification endpoint URL for this API version. */
  protected abstract URI getDefaultEndpoint();

  /** Builds the URL-encoded POST body for the given captcha solution. */
  protected abstract byte[] buildRequestBody(String solution);

  /** Adds any version-specific request headers to the builder (e.g. {@code X-API-Key} for v2). */
  protected abstract void addVersionSpecificHeaders(HttpRequest.Builder requestBuilder);

  /**
   * Parses the response body and returns {@code true} if the solution is valid, {@code false} if
   * rejected, or throws {@link FriendlyCaptchaException} on API errors.
   */
  protected abstract boolean processResponse(int statusCode, InputStream body) throws IOException;

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

  private HttpClient buildHttpClient() {
    HttpClient.Builder builder = HttpClient.newBuilder();
    if (connectTimeout != null) {
      builder.connectTimeout(connectTimeout);
    }
    if (!isEmpty(proxyHost) && proxyPort > 0) {
      builder.proxy(ProxySelector.of(new InetSocketAddress(proxyHost, proxyPort)));
      if (!isEmpty(proxyUserName) && !isEmpty(proxyPassword)) {
        builder.authenticator(new ProxyAuthenticator(proxyUserName, proxyPassword));
      }
    }
    return builder.build();
  }

  private HttpRequest buildHttpRequest(byte[] body, URI endpoint) {
    HttpRequest.Builder builder =
        HttpRequest.newBuilder()
            .uri(endpoint)
            .POST(HttpRequest.BodyPublishers.ofByteArray(body))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header("Accept", "application/json")
            .header("User-Agent", "FriendlyCaptchaJavaClient");
    if (socketTimeout != null) {
      builder.timeout(socketTimeout);
    }
    addVersionSpecificHeaders(builder);
    return builder.build();
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
