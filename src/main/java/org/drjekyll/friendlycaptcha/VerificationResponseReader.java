package org.drjekyll.friendlycaptcha;

import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
public class VerificationResponseReader {

  private final ObjectMapper objectMapper;

  /**
   * Reads and deserialises the response body into the given class using the shared ObjectMapper.
   */
  public <T> T readResponse(@NonNull InputStream inputStream, @NonNull Class<T> responseClass) {
    try {
      return objectMapper.readValue(inputStream, responseClass);
    } catch (Exception e) {
      throw new FriendlyCaptchaException("Could not read response from verification API", e);
    }
  }
}
