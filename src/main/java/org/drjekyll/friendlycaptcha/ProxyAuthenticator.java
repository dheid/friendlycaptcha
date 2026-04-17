package org.drjekyll.friendlycaptcha;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

@RequiredArgsConstructor
final class ProxyAuthenticator extends Authenticator {

  @NonNull private final String user;

  @NonNull private final String password;

  @Nullable
  @Override
  protected PasswordAuthentication getPasswordAuthentication() {
    if (getRequestorType() == RequestorType.PROXY) {
      return new PasswordAuthentication(user, password.toCharArray());
    }
    return null;
  }
}
