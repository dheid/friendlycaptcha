package org.drjekyll.friendlycaptcha;

import lombok.NonNull;
import lombok.Value;
import org.jspecify.annotations.Nullable;

@Value
class FriendlyCaptchaParams {

  @NonNull String apiKey;

  @Nullable String sitekey;
}
