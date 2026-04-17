package org.drjekyll.friendlycaptcha.v2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.drjekyll.friendlycaptcha.ErrorCode;

@Value
@Jacksonized
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorDetails {

  @JsonProperty("error_code")
  ErrorCode errorCode;
}
