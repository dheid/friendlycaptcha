package org.drjekyll.friendlycaptcha;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nonnull;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * Verifies a Friendly Captcha solution using the <b>v2 API</b>.
 *
 * <p>The API key is sent as the {@code X-API-Key} request header and the captcha response as the
 * {@code response} form field. The default endpoint is {@code
 * https://global.frcapi.com/api/v2/captcha/siteverify}.
 *
 * <p>Example:
 *
 * <pre>{@code
 * FriendlyCaptchaVerifier verifier = FriendlyCaptchaVerifierV2.builder()
 *     .apiKey("YOUR_API_KEY")
 *     .build();
 * boolean valid = verifier.verify(solution);
 * }</pre>
 */
@Slf4j
@SuperBuilder
public class FriendlyCaptchaVerifierV2 extends FriendlyCaptchaVerifier {

  private static final URI DEFAULT_ENDPOINT =
      URI.create("https://global.frcapi.com/api/v2/captcha/siteverify");

  @Override
  protected URI getDefaultEndpoint() {
    return DEFAULT_ENDPOINT;
  }

  @Override
  protected byte[] buildRequestBody(@Nonnull String solution) {
    try {
      String entity = "response=" + URLEncoder.encode(solution, "UTF-8");
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
    connection.setRequestProperty("X-API-Key", apiKey);
  }

  @Override
  protected boolean processResponse(HttpURLConnection connection, InputStream inputStream)
      throws IOException {
    VerificationResponseV2 response = readResponse(inputStream, VerificationResponseV2.class);
    int statusCode = connection.getResponseCode();
    if (verbose) {
      log.info("Received response {} with status code {}", response, statusCode);
    }
    if (statusCode == 200) {
      return response.isSuccess();
    }

    log.warn("Received response with error from Verification API: {}", response);

    VerificationResponseV2.ErrorDetails error = response.getError();
    if (error != null) {
      String message = !isEmpty(error.getDetail()) ? error.getDetail() : error.getErrorCode();
      throw new FriendlyCaptchaException(message);
    }
    throw new FriendlyCaptchaException("Verification API returned status " + statusCode);
  }
}
