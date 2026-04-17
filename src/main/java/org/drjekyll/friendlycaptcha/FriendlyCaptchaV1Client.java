package org.drjekyll.friendlycaptcha;

import static org.drjekyll.friendlycaptcha.StringUtil.isEmpty;

import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

/** Verifies a Friendly Captcha solution using the v1 API. */
@RequiredArgsConstructor
@Slf4j
class FriendlyCaptchaV1Client implements FriendlyCaptchaClient {

  private static final URI DEFAULT_ENDPOINT =
      URI.create("https://api.friendlycaptcha.com/api/v1/siteverify");

  private final FriendlyCaptchaParams friendlyCaptchaParams;

  private final VerificationResponseReader verificationResponseReader;

  @Override
  public URI getDefaultEndpoint() {
    return DEFAULT_ENDPOINT;
  }

  @Override
  public String buildRequestBody(@NonNull String solution) {
    String entity =
        "solution="
            + URLEncoder.encode(solution, StandardCharsets.UTF_8)
            + "&secret="
            + URLEncoder.encode(friendlyCaptchaParams.getApiKey(), StandardCharsets.UTF_8);
    if (!isEmpty(friendlyCaptchaParams.getSitekey())) {
      entity +=
          "&sitekey="
              + URLEncoder.encode(friendlyCaptchaParams.getSitekey(), StandardCharsets.UTF_8);
    }
    return entity;
  }

  @Override
  public boolean processResponse(int statusCode, InputStream inputStream) {
    VerificationResponseV1 response =
        verificationResponseReader.readResponse(inputStream, VerificationResponseV1.class);
    if (statusCode == 200) {
      return response.isSuccess();
    }

    log.warn("Received error response: {}", response);

    Collection<ErrorCode> errors = response.getErrors();
    if (errors == null || errors.isEmpty()) {
      throw new FriendlyCaptchaException("Verification API returned error status", statusCode);
    }

    ErrorCode errorCode = errors.iterator().next();
    String message =
        isEmpty(response.getDetails()) ? errorCode.getDescription() : response.getDetails();
    throw new FriendlyCaptchaException(message, statusCode, errorCode);
  }
}
