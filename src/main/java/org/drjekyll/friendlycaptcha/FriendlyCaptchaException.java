package org.drjekyll.friendlycaptcha;

import lombok.Getter;

@Getter
public class FriendlyCaptchaException extends RuntimeException {

  private static final long serialVersionUID = -2643367447029255633L;

  public FriendlyCaptchaException(String message) {
    this(message, null);
  }

  public FriendlyCaptchaException(String message, Throwable cause) {
    super(message, cause);
  }

}
