package org.drjekyll.friendlycaptcha;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class VerificationResponseV2 {

  private boolean success;

  private ErrorDetails error;

  @Getter
  @Setter
  @ToString
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ErrorDetails {

    @JsonProperty("error_code")
    private String errorCode;

    private String detail;
  }
}
