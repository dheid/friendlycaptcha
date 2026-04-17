package org.drjekyll.friendlycaptcha;

import static org.drjekyll.friendlycaptcha.StringUtil.isEmpty;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.ObjectMapper;

/**
 * Verifier for Friendly Captcha solutions.
 *
 * <p>Use {@link #builder()} to configure and create an instance. The API version defaults to {@link
 * FriendlyCaptchaVersion#V1}. Set {@link
 * FriendlyCaptchaVerifierBuilder#version(FriendlyCaptchaVersion)} to {@link
 * FriendlyCaptchaVersion#V2} for the v2 API.
 *
 * <p>You will need an API key that you can create on the <a
 * href="https://friendlycaptcha.com/account">Friendly Captcha account page</a>.
 *
 * <p>Example:
 *
 * <pre>{@code
 * FriendlyCaptchaVerifier verifier = FriendlyCaptchaVerifier.builder()
 *     .apiKey("YOUR_API_KEY")
 *     .build();
 * boolean valid = verifier.verify(solution);
 * }</pre>
 */
@Slf4j
public class FriendlyCaptchaVerifier {

  private final URI effectiveEndpoint;

  private final Duration socketTimeout;

  private final boolean verbose;

  private final FriendlyCaptchaClient friendlyCaptchaClient;

  private final HttpClient httpClient;

  private final String userAgent;

  /**
   * @param apiKey An API key that proves it's you, create one on the Friendly Captcha website.
   * @param objectMapper A custom Jackson object mapper if you want to use it
   * @param verificationEndpoint The URI that points to the verification API endpoint. If not set,
   *     each version uses its own default endpoint.
   * @param connectTimeout The timeout until a connection is established. A timeout value of zero is
   *     interpreted as an infinite timeout. A {@code null} value is interpreted as undefined
   *     (system default if applicable).
   * @param socketTimeout The timeout for the entire request (connecting, sending, and receiving the
   *     response). A {@code null} value means no request timeout is applied. Default: 30 seconds
   * @param sitekey An optional sitekey that you want to make sure the puzzle was generated from.
   * @param proxyHost The hostname or IP address of an optional HTTP proxy. {@code proxyPort} must
   *     be configured as well.
   * @param proxyPort The port of an HTTP proxy. Must be > 0. {@code proxyHost} must be configured
   *     as well.
   * @param proxyUserName If the HTTP proxy requires a user name for basic authentication, it can be
   *     configured here. Proxy host, port and password must also be set.
   * @param proxyPassword The corresponding password for the basic auth proxy user. The proxy host,
   *     port and user name must be set as well.
   * @param verbose Logs INFO messages with detailed information.
   * @param version The Friendly Captcha API version to use. Defaults to API version 1 (V1)
   */
  @Builder
  public FriendlyCaptchaVerifier(
      @NonNull String apiKey,
      @Nullable ObjectMapper objectMapper,
      @Nullable URI verificationEndpoint,
      @Nullable Duration connectTimeout,
      @Nullable Duration socketTimeout,
      @Nullable String sitekey,
      @Nullable String proxyHost,
      int proxyPort,
      @Nullable String proxyUserName,
      @Nullable String proxyPassword,
      @Nullable String userAgent,
      boolean verbose,
      FriendlyCaptchaVersion version) {
    StringUtil.assertNotEmpty(apiKey, "API key must not be null or empty");
    this.socketTimeout = socketTimeout;
    this.userAgent = userAgent == null ? "FriendlyCaptchaJavaClient" : userAgent;
    this.verbose = verbose;
    VerificationResponseReader verificationResponseReader =
        new VerificationResponseReader(objectMapper == null ? new ObjectMapper() : objectMapper);
    FriendlyCaptchaParams friendlyCaptchaParams = new FriendlyCaptchaParams(apiKey, sitekey);
    if (version == FriendlyCaptchaVersion.V2) {
      this.friendlyCaptchaClient =
          new FriendlyCaptchaV2Client(friendlyCaptchaParams, verificationResponseReader);
    } else {
      this.friendlyCaptchaClient =
          new FriendlyCaptchaV1Client(friendlyCaptchaParams, verificationResponseReader);
    }
    this.effectiveEndpoint =
        verificationEndpoint == null
            ? friendlyCaptchaClient.getDefaultEndpoint()
            : requireHttpVerificationEndpointScheme(verificationEndpoint);
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
    this.httpClient = builder.build();
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
  public boolean verify(@NonNull String solution) {
    StringUtil.assertNotEmpty(solution, "Solution must not be null or empty");
    if (verbose) {
      log.info("Verifying friendly captcha solution using endpoint {}", effectiveEndpoint);
    }
    try {
      HttpResponse<InputStream> response =
          httpClient.send(buildHttpRequest(solution), HttpResponse.BodyHandlers.ofInputStream());
      if (verbose) {
        log.info("Received response {} with status code {}", response, response.statusCode());
      }
      return friendlyCaptchaClient.processResponse(response.statusCode(), response.body());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new FriendlyCaptchaException("Interrupted while checking solution", e);
    } catch (IOException e) {
      throw new FriendlyCaptchaException("Could not check solution", e);
    }
  }

  /**
   * Verifies the given captcha solution against the Friendly Captcha API asynchronously.
   *
   * <p>The returned future completes with {@code true} if the solution is valid, or {@code false}
   * if it was rejected. It completes exceptionally with a {@link
   * java.util.concurrent.CompletionException} whose cause is always a {@link
   * FriendlyCaptchaException} — network failures are wrapped in one, consistent with {@link
   * #verify(String)}.
   *
   * @param solution the captcha response value submitted by the user
   * @return a future that resolves to {@code true} if the solution is accepted, {@code false} if
   *     rejected
   * @throws IllegalArgumentException if solution is null or empty
   */
  public CompletableFuture<Boolean> verifyAsync(@NonNull String solution) {
    StringUtil.assertNotEmpty(solution, "Solution must not be null or empty");
    if (verbose) {
      log.info("Verifying friendly captcha solution using endpoint {}", effectiveEndpoint);
    }
    return httpClient
        .sendAsync(buildHttpRequest(solution), HttpResponse.BodyHandlers.ofInputStream())
        .thenApply(
            response -> {
              if (verbose) {
                log.info(
                    "Received response {} with status code {}", response, response.statusCode());
              }
              return friendlyCaptchaClient.processResponse(response.statusCode(), response.body());
            })
        .exceptionallyCompose(
            ex -> {
              Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
              if (cause instanceof FriendlyCaptchaException fce) {
                return CompletableFuture.failedFuture(fce);
              }
              return CompletableFuture.failedFuture(
                  new FriendlyCaptchaException("Could not check solution", cause));
            });
  }

  private HttpRequest buildHttpRequest(String solution) {
    HttpRequest.Builder builder =
        HttpRequest.newBuilder()
            .uri(effectiveEndpoint)
            .POST(
                HttpRequest.BodyPublishers.ofString(
                    friendlyCaptchaClient.buildRequestBody(solution)))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header("Accept", "application/json")
            .header("User-Agent", userAgent);
    if (socketTimeout != null) {
      builder.timeout(socketTimeout);
    }
    friendlyCaptchaClient.addVersionSpecificHeaders(builder);
    return builder.build();
  }

  private static URI requireHttpVerificationEndpointScheme(@NonNull URI endpoint) {
    String scheme = endpoint.getScheme();
    if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
      return endpoint;
    }
    throw new FriendlyCaptchaException("Invalid verification endpoint URL");
  }
}
