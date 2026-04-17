package org.drjekyll.friendlycaptcha;

import static org.drjekyll.friendlycaptcha.StringUtil.isEmpty;

import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

/** Verifies a Friendly Captcha solution using the v2 API. */
@RequiredArgsConstructor
@Slf4j
class FriendlyCaptchaV2Client implements FriendlyCaptchaClient {

  private static final URI DEFAULT_ENDPOINT =
      URI.create("https://global.frcapi.com/api/v2/captcha/siteverify");

  private final FriendlyCaptchaParams friendlyCaptchaParams;

  private final VerificationResponseReader verificationResponseReader;

  @Override
  public URI getDefaultEndpoint() {
    return DEFAULT_ENDPOINT;
  }

  @Override
  public String buildRequestBody(@NonNull String solution) {
    String entity = "response=" + URLEncoder.encode(solution, StandardCharsets.UTF_8);
    if (!isEmpty(friendlyCaptchaParams.getSitekey())) {
      entity +=
          "&sitekey="
              + URLEncoder.encode(friendlyCaptchaParams.getSitekey(), StandardCharsets.UTF_8);
    }
    return entity;
  }

  @Override
  public void addVersionSpecificHeaders(HttpRequest.Builder requestBuilder) {
    requestBuilder.header("X-API-Key", friendlyCaptchaParams.getApiKey());
  }

  @Override
  public boolean processResponse(int statusCode, InputStream inputStream) {
    VerificationResponseV2 response =
        verificationResponseReader.readResponse(inputStream, VerificationResponseV2.class);
    if (statusCode == 200) {
      return response.isSuccess();
    }

    log.warn("Received error response: {}", response);

    ErrorDetails error = response.getError();
    if (error == null || error.getErrorCode() == null) {
      throw new FriendlyCaptchaException("Verification API returned error status", statusCode);
    }
    throw new FriendlyCaptchaException(
        error.getErrorCode().getDescription(), statusCode, error.getErrorCode());
  }
}
