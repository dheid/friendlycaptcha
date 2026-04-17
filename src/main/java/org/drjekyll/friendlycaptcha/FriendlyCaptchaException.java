package org.drjekyll.friendlycaptcha;

import java.io.Serial;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

@Getter
public class FriendlyCaptchaException extends RuntimeException {

  @Serial private static final long serialVersionUID = -2643367447029255633L;

  /**
   * The HTTP status code returned by the verification API, or {@code null} if the exception was not
   * caused by an HTTP response (e.g. a network error or invalid configuration).
   */
  @Nullable private final Integer statusCode;

  @Nullable private final ErrorCode errorCode;

  public FriendlyCaptchaException(String message) {
    this(message, null, null, null);
  }

  public FriendlyCaptchaException(String message, @Nullable Throwable cause) {
    this(message, null, null, cause);
  }

  public FriendlyCaptchaException(String message, int statusCode) {
    this(message, statusCode, null, null);
  }

  public FriendlyCaptchaException(String message, int statusCode, @Nullable ErrorCode errorCode) {
    this(message, statusCode, errorCode, null);
  }

  public FriendlyCaptchaException(
      String message, int statusCode, @Nullable ErrorCode errorCode, @Nullable Throwable cause) {
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
