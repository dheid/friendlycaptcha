package org.drjekyll.friendlycaptcha;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VerificationError {

  @JsonProperty("secret_missing")
  SECRET_MISSING("No secret API key transmitted"),

  @JsonProperty("secret_invalid")
  SECRET_INVALID("Provided secret API key invalid"),

  @JsonProperty("solution_missing")
  SOLUTION_MISSING("No solution provided"),

  @JsonProperty("bad_request")
  BAD_REQUEST("The verification HTTP POST request was invalid"),

  @JsonProperty("solution_invalid")
  SOLUTION_INVALID("The provided solution was invalid"),

  @JsonProperty("solution_timeout_or_duplicate")
  SOLUTION_TIMEOUT_OR_DUPLICATE("The solution has expired or already been used"),

  ;

  private final String description;

}
