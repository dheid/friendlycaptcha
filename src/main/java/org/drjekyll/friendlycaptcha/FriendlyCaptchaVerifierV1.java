package org.drjekyll.friendlycaptcha;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import javax.annotation.Nonnull;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * Verifies a Friendly Captcha solution using the <b>v1 API</b>.
 *
 * <p>The API key is sent as the {@code secret} form field and the captcha response as {@code
 * solution}. The default endpoint is {@code https://api.friendlycaptcha.com/api/v1/siteverify}.
 *
 * <p>Example:
 *
 * <pre>{@code
 * FriendlyCaptchaVerifier verifier = FriendlyCaptchaVerifierV1.builder()
 *     .apiKey("YOUR_API_KEY")
 *     .build();
 * boolean valid = verifier.verify(solution);
 * }</pre>
 */
@Slf4j
@SuperBuilder
public class FriendlyCaptchaVerifierV1 extends FriendlyCaptchaVerifier {

  private static final URI DEFAULT_ENDPOINT =
      URI.create("https://api.friendlycaptcha.com/api/v1/siteverify");

  @Override
  protected URI getDefaultEndpoint() {
    return DEFAULT_ENDPOINT;
  }

  @Override
  protected byte[] buildRequestBody(@Nonnull String solution) {
    try {
      String entity =
          "solution="
              + URLEncoder.encode(solution, "UTF-8")
              + "&secret="
              + URLEncoder.encode(apiKey, "UTF-8");
      if (!isEmpty(sitekey)) {
        entity += "&sitekey=" + URLEncoder.encode(sitekey, "UTF-8");
      }
      return entity.getBytes(StandardCharsets.UTF_8);
    } catch (UnsupportedEncodingException e) {
      throw new FriendlyCaptchaException("Could not encode payload", e);
    }
  }

  @Override
  protected void configureVersionSpecificHeaders(HttpURLConnection connection) {
    // v1 authenticates via body field — no additional headers required
  }

  @Override
  protected boolean processResponse(HttpURLConnection connection, InputStream inputStream)
      throws IOException {
    VerificationResponse response = readResponse(inputStream, VerificationResponse.class);
    if (verbose) {
      log.info("Received response {} with status code {}", response, connection.getResponseCode());
    }
    if (connection.getResponseCode() == 200) {
      return response.isSuccess();
    }

    Collection<VerificationError> errors = response.getErrors();
    if (errors == null || errors.isEmpty()) {
      throw new FriendlyCaptchaException("Verification API did not return any error");
    }

    log.warn("Received response with errors from Verification API: {}", response);

    VerificationError verificationError = errors.iterator().next();
    String message =
        isEmpty(response.getDetails()) ? verificationError.getDescription() : response.getDetails();
    throw new FriendlyCaptchaException(message);
  }
}
