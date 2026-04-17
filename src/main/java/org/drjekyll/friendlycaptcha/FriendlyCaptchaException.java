package org.drjekyll.friendlycaptcha;

import java.io.Serial;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/**
 * Thrown when the Friendly Captcha API returns an error response, when the response cannot be
 * parsed, or when a network-level failure prevents verification from completing.
 *
 * <p>Callers should distinguish this from a plain {@code false} return value: a {@code false} means
 * the solution was rejected (invalid, expired, or already used), while this exception means the
 * verification request itself failed.
 *
 * <p>{@link #getStatusCode()} returns the HTTP status code when the API responded with an error, or
 * {@code null} for non-HTTP failures (network errors, unreadable responses, invalid configuration).
 * {@link #getErrorCode()} returns the machine-readable error code when the API included one in its
 * response body.
 */
@Getter
public class FriendlyCaptchaException extends RuntimeException {

  @Serial private static final long serialVersionUID = -2643367447029255633L;

  /**
   * The HTTP status code returned by the verification API, or {@code null} if the exception was not
   * caused by an HTTP response (e.g. a network error or invalid configuration).
   */
  @Nullable private final Integer statusCode;

  /**
   * The machine-readable error code included in the API response body, or {@code null} if the API
   * did not return one or the exception was not caused by an API error response.
   */
  @Nullable private final ErrorCode errorCode;

  FriendlyCaptchaException(@Nullable String message) {
    this(message, null, null, null);
  }

  FriendlyCaptchaException(@Nullable String message, @Nullable Throwable cause) {
    this(message, null, null, cause);
  }

  FriendlyCaptchaException(@Nullable String message, int statusCode) {
    this(message, statusCode, null, null);
  }

  FriendlyCaptchaException(
      @Nullable String message, int statusCode, @Nullable ErrorCode errorCode) {
    this(message, statusCode, errorCode, null);
  }

  FriendlyCaptchaException(
      @Nullable String message,
      int statusCode,
      @Nullable ErrorCode errorCode,
      @Nullable Throwable cause) {
    this(message, Integer.valueOf(statusCode), errorCode, cause);
  }

  private FriendlyCaptchaException(
      String message,
      @Nullable Integer statusCode,
      @Nullable ErrorCode errorCode,
      @Nullable Throwable cause) {
    super(message, cause);
    this.statusCode = statusCode;
    this.errorCode = errorCode;
  }
}
