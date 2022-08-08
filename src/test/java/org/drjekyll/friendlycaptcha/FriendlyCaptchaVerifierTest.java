package org.drjekyll.friendlycaptcha;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.net.URI;
import org.junit.jupiter.api.Test;

@WireMockTest(httpPort = 8080)
class FriendlyCaptchaVerifierTest {

  public static final String VALID_API_KEY =
    "B191X90HRE6PA37HDSUIMXS6L46HQGL1A5PGJBFQ12VCV52GTI4HJA2CGI";

  public static final String SITEKEY = "NQTVT3JKLX8WX1VQ";

  private final FriendlyCaptchaVerifier.FriendlyCaptchaVerifierBuilder frcSolutionValidatorBuilder =
    FriendlyCaptchaVerifier.builder().verificationEndpoint(URI.create("http://localhost:8080"));

  private FriendlyCaptchaVerifier friendlyCaptchaVerifier;

  private boolean valid;

  @Test
  void requiresSecret() {

    assertThatThrownBy(frcSolutionValidatorBuilder::build)
      .isInstanceOf(NullPointerException.class)
      .hasMessage("apiKey is marked non-null but is null");

  }

  @Test
  void validSolutionIsValid() {

    friendlyCaptchaVerifier =
      frcSolutionValidatorBuilder.apiKey(VALID_API_KEY).sitekey(SITEKEY).build();

    whenValidatesSolution(
      "93b573652d6aefb0496856a0e928661c.YlWWwKqT3fMFowr/AQwwngAAAAAAAAAA3evEukjCNZE=.AAAAAHSLAQABAAAAmgEBAAIAAABd2gAAAwAAAK/1FwAEAAAAKmMPAAUAAACUBgEABgAAAGhAEAAHAAAAk20FAAgAAAAzWwgACQAAAIxKEwAKAAAAiK4NAAsAAACtYgkADAAAAIb3AwANAAAAM10CAA4AAAA/2gIADwAAAETWDQAQAAAA4hMEABEAAACBjwgAEgAAALgHBAATAAAA1S8CABQAAACiNAMAFQAAAMpGGgAWAAAAokIVABcAAAAJxQgAGAAAAKgMCQAZAAAA0aMJABoAAACJKAgAGwAAAC25BwAcAAAA3tALAB0AAAB8kQQAHgAAAHlVFAAfAAAAMDgnACAAAAAesRcAIQAAACRXHQAiAAAA0hYLACMAAADS5iYAJAAAAE39AwAlAAAAZkIAACYAAACl/BQAJwAAAM5+BAAoAAAAg6sTACkAAABKpAsAKgAAANTHFgArAAAAOwgLACwAAAA1EAkALQAAAPnrGwAuAAAAXdgyAC8AAAAVQAoA.AgAF");

    assertThat(valid).isTrue();

  }

  @Test
  void syntacticallyIncorrectSolutionIsInvalid() {

    friendlyCaptchaVerifier = frcSolutionValidatorBuilder.apiKey(VALID_API_KEY).build();

    whenValidatesSolution("test");

    thenSolutionIsInvalid();

  }

  @Test
  void failsOnMissingSecret() {

    friendlyCaptchaVerifier = frcSolutionValidatorBuilder.apiKey("").build();

    assertThatThrownBy(() -> whenValidatesSolution("test"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("API key must not be null or empty");

  }

  @Test
  void failsOnMissingSolution() {

    friendlyCaptchaVerifier = frcSolutionValidatorBuilder.apiKey(VALID_API_KEY).build();

    assertThatThrownBy(() -> whenValidatesSolution(""))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Solution must not be null or empty");

  }

  @Test
  void failsOnInvalidSecret() {

    friendlyCaptchaVerifier = frcSolutionValidatorBuilder.apiKey("invalid").build();

    assertThatThrownBy(() -> whenValidatesSolution("test")).isInstanceOf(
      FriendlyCaptchaException.class).hasMessage(
      "Provided secret API key invalid");

  }

  private void thenSolutionIsInvalid() {
    assertThat(valid).isFalse();
  }

  private void whenValidatesSolution(String solution) {
    valid = friendlyCaptchaVerifier.verify(solution);
  }

}
