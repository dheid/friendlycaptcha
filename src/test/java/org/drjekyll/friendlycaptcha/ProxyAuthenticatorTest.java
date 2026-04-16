package org.drjekyll.friendlycaptcha;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProxyAuthenticatorTest {

  @BeforeEach
  void setUp() {
    Authenticator.setDefault(new ProxyAuthenticator("testuser", "testpass"));
  }

  @AfterEach
  void tearDown() {
    Authenticator.setDefault(null);
  }

  @Test
  void returnsCredentialsForProxyRequests() {
    PasswordAuthentication auth =
        Authenticator.requestPasswordAuthentication(
            "localhost", null, 8080, "http", "", "basic", null, Authenticator.RequestorType.PROXY);
    assertThat(auth).isNotNull();
    assertThat(auth.getUserName()).isEqualTo("testuser");
    assertThat(new String(auth.getPassword())).isEqualTo("testpass");
  }

  @Test
  void returnsNullForNonProxyRequests() {
    PasswordAuthentication auth =
        Authenticator.requestPasswordAuthentication(
            "localhost", null, 8080, "http", "", "basic", null, Authenticator.RequestorType.SERVER);
    assertThat(auth).isNull();
  }
}
