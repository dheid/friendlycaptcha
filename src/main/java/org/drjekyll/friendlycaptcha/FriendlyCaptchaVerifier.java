package org.drjekyll.friendlycaptcha;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

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
    assertNotEmpty(apiKey, "Secret must not be null or empty");
    assertVerificationEndpointScheme();

    HttpEntity entity = createEntity(solution);
    HttpClientBuilder httpClientBuilder = createHttpClientBuilder();
    HttpPost request = createRequest(entity);

    if (verbose) {
      log.info("Verifying friendly captcha solution using endpoint {}", verificationEndpoint);
    }
    try (
      CloseableHttpClient httpClient = httpClientBuilder.build();
      CloseableHttpResponse response = httpClient.execute(request)
    ) {
      VerificationResponse verificationResponse = readVerificationResponse(response);
      if (verbose) {
        log.info("Received response {} with status code {}", verificationResponse, response.getStatusLine().getStatusCode());
      }
      if (response.getStatusLine().getStatusCode() == 200) {
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

    } catch (IOException e) {
      throw new FriendlyCaptchaException("Could not check solution", e);
    }

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
  private HttpEntity createEntity(@Nonnull String solution) {
    assertNotEmpty(solution, "Solution must not be null or empty");
    List<NameValuePair> params = new ArrayList<>(3);
    params.add(new BasicNameValuePair("solution", solution));
    params.add(new BasicNameValuePair("secret", apiKey));
    if (!isEmpty(sitekey)) {
      params.add(new BasicNameValuePair("sitekey", sitekey));
    }
    try {
      return new UrlEncodedFormEntity(params);
    } catch (UnsupportedEncodingException e) {
      throw new FriendlyCaptchaException("Could not create form entity", e);
    }
  }

  private HttpClientBuilder createHttpClientBuilder() {
    RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
    if (connectTimeout != null) {
      requestConfigBuilder.setConnectTimeout((int) connectTimeout.toMillis());
    }
    if (socketTimeout != null) {
      requestConfigBuilder.setSocketTimeout((int) socketTimeout.toMillis());
    }
    HttpClientBuilder httpClientBuilder = HttpClientBuilder.create().setUserAgent(
      "FriendlyCaptchaJavaClient").setDefaultRequestConfig(requestConfigBuilder.build());
    if (!isEmpty(proxyHost) && proxyPort > 0) {
      httpClientBuilder.setProxy(new HttpHost(proxyHost, proxyPort));
      if (!isEmpty(proxyUserName) && !isEmpty(proxyPassword)) {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        AuthScope authScope = new AuthScope(proxyHost, proxyPort);
        credentialsProvider.setCredentials(authScope,
          new UsernamePasswordCredentials(proxyUserName, proxyPassword)
        );
        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
      }
    }
    return httpClientBuilder;
  }

  private HttpPost createRequest(HttpEntity entity) {
    HttpPost request = new HttpPost(verificationEndpoint);
    request.setEntity(entity);
    request.addHeader("Accept", "application/json");
    request.addHeader("Content-Type", "application/x-www-form-urlencoded");
    return request;
  }

  private VerificationResponse readVerificationResponse(@Nonnull HttpResponse response) {
    try {
      return objectMapper.readValue(response.getEntity().getContent(), VerificationResponse.class);
    } catch (Exception e) {
      throw new FriendlyCaptchaException("Could not read response from verification API", e);
    }
  }

  private static boolean isEmpty(@Nullable String str) {
    return str == null || str.trim().isEmpty();
  }

}
