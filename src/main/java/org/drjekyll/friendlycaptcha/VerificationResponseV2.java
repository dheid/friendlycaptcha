package org.drjekyll.friendlycaptcha;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Jacksonized
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
class VerificationResponseV2 {

  boolean success;

  ErrorDetails error;
}
