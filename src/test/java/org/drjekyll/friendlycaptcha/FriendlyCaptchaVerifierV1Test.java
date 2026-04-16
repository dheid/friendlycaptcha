package org.drjekyll.friendlycaptcha;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.net.URI;
import org.junit.jupiter.api.Test;

@WireMockTest(httpPort = 8080)
class FriendlyCaptchaVerifierV1Test {

  private static final String VALID_API_KEY =
      "B191X90HRE6PA37HDSUIMXS6L46HQGL1A5PGJBFQ12VCV52GTI4HJA2CGI";

  private static final String SITEKEY = "NQTVT3JKLX8WX1VQ";

  private static final URI LOCALHOST = URI.create("http://localhost:8080");

  private FriendlyCaptchaVerifier verifier;

  private boolean valid;

  @Test
  void requiresApiKey() {

    assertThatThrownBy(() -> FriendlyCaptchaVerifierV1.builder().build())
        .isInstanceOf(NullPointerException.class)
        .hasMessage("apiKey is marked non-null but is null");
  }

  @Test
  void validSolutionIsValid() {

    verifier =
        FriendlyCaptchaVerifierV1.builder()
            .verificationEndpoint(LOCALHOST)
            .apiKey(VALID_API_KEY)
            .sitekey(SITEKEY)
            .build();

    whenValidatesSolution(
        "93b573652d6aefb0496856a0e928661c.YlWWwKqT3fMFowr/AQwwngAAAAAAAAAA3evEukjCNZE=.AAAAAHSLAQABAAAAmgEBAAIAAABd2gAAAwAAAK/1FwAEAAAAKmMPAAUAAACUBgEABgAAAGhAEAAHAAAAk20FAAgAAAAzWwgACQAAAIxKEwAKAAAAiK4NAAsAAACtYgkADAAAAIb3AwANAAAAM10CAA4AAAA/2gIADwAAAETWDQAQAAAA4hMEABEAAACBjwgAEgAAALgHBAATAAAA1S8CABQAAACiNAMAFQAAAMpGGgAWAAAAokIVABcAAAAJxQgAGAAAAKgMCQAZAAAA0aMJABoAAACJKAgAGwAAAC25BwAcAAAA3tALAB0AAAB8kQQAHgAAAHlVFAAfAAAAMDgnACAAAAAesRcAIQAAACRXHQAiAAAA0hYLACMAAADS5iYAJAAAAE39AwAlAAAAZkIAACYAAACl/BQAJwAAAM5+BAAoAAAAg6sTACkAAABKpAsAKgAAANTHFgArAAAAOwgLACwAAAA1EAkALQAAAPnrGwAuAAAAXdgyAC8AAAAVQAoA.AgAF");

    assertThat(valid).isTrue();
  }

  @Test
  void syntacticallyIncorrectSolutionIsInvalid() {

    verifier =
        FriendlyCaptchaVerifierV1.builder()
            .verificationEndpoint(LOCALHOST)
            .apiKey(VALID_API_KEY)
            .build();

    whenValidatesSolution("test");

    assertThat(valid).isFalse();
  }

  @Test
  void failsOnEmptyApiKey() {

    verifier =
        FriendlyCaptchaVerifierV1.builder().verificationEndpoint(LOCALHOST).apiKey("").build();

    assertThatThrownBy(() -> whenValidatesSolution("test"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("API key must not be null or empty");
  }

  @Test
  void failsOnEmptySolution() {

    verifier =
        FriendlyCaptchaVerifierV1.builder()
            .verificationEndpoint(LOCALHOST)
            .apiKey(VALID_API_KEY)
            .build();

    assertThatThrownBy(() -> whenValidatesSolution(""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Solution must not be null or empty");
  }

  @Test
  void failsOnInvalidApiKey() {

    verifier =
        FriendlyCaptchaVerifierV1.builder()
            .verificationEndpoint(LOCALHOST)
            .apiKey("invalid")
            .build();

    assertThatThrownBy(() -> whenValidatesSolution("test"))
        .isInstanceOf(FriendlyCaptchaException.class)
        .hasMessage("Provided secret API key invalid");
  }

  @Test
  void sitekeyWithSpecialCharactersIsUrlEncoded() {

    stubFor(
        post("/")
            .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
            .withRequestBody(
                equalTo("solution=test&secret=" + VALID_API_KEY + "&sitekey=NQT%2BVT3%2FJKLX"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"success\":true}")));

    verifier =
        FriendlyCaptchaVerifierV1.builder()
            .verificationEndpoint(LOCALHOST)
            .apiKey(VALID_API_KEY)
            .sitekey("NQT+VT3/JKLX")
            .build();

    whenValidatesSolution("test");

    assertThat(valid).isTrue();
  }

  @Test
  void handlesErrorResponseWithoutBody() {

    stubFor(post("/").willReturn(aResponse().withStatus(500)));

    verifier =
        FriendlyCaptchaVerifierV1.builder()
            .verificationEndpoint(LOCALHOST)
            .apiKey(VALID_API_KEY)
            .build();

    assertThatThrownBy(() -> whenValidatesSolution("test"))
        .isInstanceOf(FriendlyCaptchaException.class);
  }

  @Test
  void rejectsNonHttpEndpoint() {

    verifier =
        FriendlyCaptchaVerifierV1.builder()
            .verificationEndpoint(URI.create("ftp://example.com"))
            .apiKey(VALID_API_KEY)
            .build();

    assertThatThrownBy(() -> whenValidatesSolution("test"))
        .isInstanceOf(FriendlyCaptchaException.class)
        .hasMessage("Invalid verification endpoint URL");
  }

  @Test
  void includesDetailsInExceptionWhenPresent() {

    stubFor(
        post("/")
            .willReturn(
                aResponse()
                    .withStatus(400)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{\"success\":false,\"errors\":[\"solution_invalid\"],\"details\":\"Detailed error message\"}")));

    verifier =
        FriendlyCaptchaVerifierV1.builder()
            .verificationEndpoint(LOCALHOST)
            .apiKey(VALID_API_KEY)
            .build();

    assertThatThrownBy(() -> whenValidatesSolution("test"))
        .isInstanceOf(FriendlyCaptchaException.class)
        .hasMessage("Detailed error message");
  }

  private void whenValidatesSolution(String solution) {
    valid = verifier.verify(solution);
  }
}
