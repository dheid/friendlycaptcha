package org.drjekyll.friendlycaptcha;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.net.URI;
import org.junit.jupiter.api.Test;

/**
 * Verifies that the deprecated {@link FriendlyCaptchaVerifier#builder()} still compiles, builds,
 * and behaves identically to {@link FriendlyCaptchaVerifierV1#builder()}.
 */
@SuppressWarnings("deprecation")
@WireMockTest(httpPort = 8080)
class FriendlyCaptchaVerifierBackwardsCompatibilityTest {

  private static final String VALID_API_KEY =
      "B191X90HRE6PA37HDSUIMXS6L46HQGL1A5PGJBFQ12VCV52GTI4HJA2CGI";

  @Test
  void builderReturnsV1Verifier() {

    FriendlyCaptchaVerifier verifier =
        FriendlyCaptchaVerifier.builder()
            .verificationEndpoint(URI.create("http://localhost:8080"))
            .apiKey(VALID_API_KEY)
            .build();

    assertThat(verifier).isInstanceOf(FriendlyCaptchaVerifierV1.class);
  }

  @Test
  void builderRequiresApiKey() {

    assertThatThrownBy(() -> FriendlyCaptchaVerifier.builder().build())
        .isInstanceOf(NullPointerException.class)
        .hasMessage("apiKey is marked non-null but is null");
  }
}
