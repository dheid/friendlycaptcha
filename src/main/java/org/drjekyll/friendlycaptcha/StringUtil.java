package org.drjekyll.friendlycaptcha;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

class StringUtil {

  static boolean isEmpty(@Nullable String str) {
    return str == null || str.isEmpty() || str.trim().isEmpty();
  }

  static void assertNotEmpty(@NonNull String str, @Nullable String message) {
    if (isEmpty(str)) {
      throw new IllegalArgumentException(message);
    }
  }
}
