package org.drjekyll.friendlycaptcha;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;

class FriendlyCaptchaVerifierTest {

  private static final String VALID_API_KEY =
      "B191X90HRE6PA37HDSUIMXS6L46HQGL1A5PGJBFQ12VCV52GTI4HJA2CGI";

  private FriendlyCaptchaVerifier verifier;

  @Test
  void failsOnInvalidEndpoint() {

    verifier =
        FriendlyCaptchaVerifier.builder()
            .verificationEndpoint(URI.create("http://localhost:1234"))
            .apiKey(VALID_API_KEY)
            .build();

    assertThatThrownBy(() -> verifier.verify("test"))
        .isInstanceOf(FriendlyCaptchaException.class)
        .hasMessage("Could not check solution");
  }

  @Test
  void requiresApiKey() {

    assertThatThrownBy(() -> FriendlyCaptchaVerifier.builder().build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("API key must not be null or empty");
  }

  @Test
  void failsOnEmptyApiKey() {

    assertThatThrownBy(() -> FriendlyCaptchaVerifier.builder().apiKey("").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("API key must not be null or empty");
  }

  @Test
  void failsOnInvalidEndpointAsync() {

    verifier =
        FriendlyCaptchaVerifier.builder()
            .verificationEndpoint(URI.create("http://localhost:1234"))
            .apiKey(VALID_API_KEY)
            .build();

    CompletableFuture<Boolean> future = verifier.verifyAsync("test");

    assertThatThrownBy(future::get)
        .isInstanceOf(ExecutionException.class)
        .cause()
        .isInstanceOf(FriendlyCaptchaException.class)
        .hasMessage("Could not check solution");
  }

  @Test
  void rejectsEmptySolutionAsync() {

    verifier = FriendlyCaptchaVerifier.builder().apiKey(VALID_API_KEY).build();

    assertThatThrownBy(() -> verifier.verifyAsync(""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Solution must not be null or empty");
  }

  @Test
  void rejectsNonHttpEndpoint() {

    assertThatThrownBy(
            () ->
                FriendlyCaptchaVerifier.builder()
                    .version(FriendlyCaptchaVersion.V1)
                    .verificationEndpoint(URI.create("ftp://example.com"))
                    .apiKey(VALID_API_KEY)
                    .build())
        .isInstanceOf(FriendlyCaptchaException.class)
        .hasMessage("Invalid verification endpoint URL");
  }
}
