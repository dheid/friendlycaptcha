package org.drjekyll.friendlycaptcha;

import lombok.NonNull;
import lombok.Value;
import org.jspecify.annotations.Nullable;

@Value
public class FriendlyCaptchaParams {

  /** An API key that proves it's you, create one on the Friendly Captcha website. */
  @NonNull String apiKey;

  /** An optional sitekey that you want to make sure the puzzle was generated from. */
  @Nullable String sitekey;
}
