package org.drjekyll.friendlycaptcha;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Objects;

@RequiredArgsConstructor
final class ProxyAuthenticator extends Authenticator {

  @NonNull
  private final String user;

  @NonNull
  private final String password;

  @Nullable
  @Override
  protected PasswordAuthentication getPasswordAuthentication() {
    if (getRequestorType() == RequestorType.PROXY) {
      return new PasswordAuthentication(user, password.toCharArray());
    }
    return null;
  }

}
