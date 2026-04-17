package org.drjekyll.friendlycaptcha;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Machine-readable error codes returned by the Friendly Captcha API in an error response body.
 *
 * <p>These are surfaced via {@link FriendlyCaptchaException#getErrorCode()} when the API includes
 * an error code in its response. {@link #getDescription()} returns a human-readable explanation
 * suitable for logging.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  /** The v1 API secret key was not included in the request (v1 only). */
  @JsonProperty("secret_missing")
  SECRET_MISSING("No secret API key transmitted"),

  /** The v1 API secret key was present but not valid (v1 only). */
  @JsonProperty("secret_invalid")
  SECRET_INVALID("Provided secret API key invalid"),

  /** The captcha solution field was missing from the request body (v1 only). */
  @JsonProperty("solution_missing")
  SOLUTION_MISSING("No solution provided"),

  /** The POST request was malformed or missing required fields. */
  @JsonProperty("bad_request")
  BAD_REQUEST("The verification HTTP POST request was invalid"),

  /** The submitted solution did not pass verification (v1 only). */
  @JsonProperty("solution_invalid")
  SOLUTION_INVALID("The provided solution was invalid"),

  /** The solution was valid but has already been used or has expired (v1 only). */
  @JsonProperty("solution_timeout_or_duplicate")
  SOLUTION_TIMEOUT_OR_DUPLICATE("The solution has expired or already been used"),

  /** The sitekey supplied with the request does not match any known sitekey. */
  @JsonProperty("sitekey_invalid")
  SITEKEY_INVALID("The provided sitekey was invalid"),

  /** No API key was supplied in the request (v2 only). */
  @JsonProperty("auth_required")
  AUTH_REQUIRED("Missing API key"),

  /** The API key supplied in the request is not valid (v2 only). */
  @JsonProperty("auth_invalid")
  AUTH_INVALID("The provided API key was invalid"),

  /** The {@code response} field was missing from the request body (v2 only). */
  @JsonProperty("response_missing")
  RESPONSE_MISSING("Response parameter is missing"),

  /** The submitted response token did not pass verification (v2 only). */
  @JsonProperty("response_invalid")
  RESPONSE_INVALID("Invalid response provided"),

  /** The response token was valid but has expired (v2 only). */
  @JsonProperty("response_timeout")
  REQUEST_TIMEOUT("The response has expired"),

  /** The response token was valid but has already been used (v2 only). */
  @JsonProperty("response_duplicate")
  RESPONSE_DUPLICATE("The response has already been used"),
  ;

  /** Human-readable description of this error, suitable for logging. */
  private final String description;
}
