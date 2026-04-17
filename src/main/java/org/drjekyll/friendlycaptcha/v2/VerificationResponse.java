package org.drjekyll.friendlycaptcha.v2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Jacksonized
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
class VerificationResponse {

  boolean success;

  ErrorDetails error;
}
