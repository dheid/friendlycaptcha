package org.drjekyll.friendlycaptcha;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;

interface FriendlyCaptchaClient {

  /** Returns the default verification endpoint URL for this API version. */
  URI getDefaultEndpoint();

  /** Builds the URL-encoded POST body for the given captcha solution. */
  String buildRequestBody(String solution);

  /** Adds any version-specific request headers to the builder (e.g. {@code X-API-Key} for v2). */
  default void addVersionSpecificHeaders(HttpRequest.Builder requestBuilder) {
    // only if additional headers are required
  }

  /**
   * Parses the response body and returns {@code true} if the solution is valid, {@code false} if
   * rejected, or throws {@link FriendlyCaptchaException} on API errors.
   */
  boolean processResponse(int statusCode, InputStream body);
}
