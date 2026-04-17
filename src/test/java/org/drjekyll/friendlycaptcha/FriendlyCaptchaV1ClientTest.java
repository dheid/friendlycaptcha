package org.drjekyll.friendlycaptcha;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.Test;

@WireMockTest(httpPort = 8080)
class FriendlyCaptchaV1ClientTest {

  private static final String VALID_API_KEY =
      "B191X90HRE6PA37HDSUIMXS6L46HQGL1A5PGJBFQ12VCV52GTI4HJA2CGI";

  private static final String SITEKEY = "NQTVT3JKLX8WX1VQ";

  private static final URI LOCALHOST = URI.create("HTTP://localhost:8080");

  private FriendlyCaptchaVerifier verifier;

  private boolean valid;

  @Test
  void validSolutionIsValid() {

    verifier =
        FriendlyCaptchaVerifier.builder()
            .version(FriendlyCaptchaVersion.V1)
            .verificationEndpoint(LOCALHOST)
            .apiKey(VALID_API_KEY)
            .sitekey(SITEKEY)
            .connectTimeout(Duration.ofMinutes(10))
            .build();

    whenValidatesSolution(
        "93b573652d6aefb0496856a0e928661c.YlWWwKqT3fMFowr/AQwwngAAAAAAAAAA3evEukjCNZE=.AAAAAHSLAQABAAAAmgEBAAIAAABd2gAAAwAAAK/1FwAEAAAAKmMPAAUAAACUBgEABgAAAGhAEAAHAAAAk20FAAgAAAAzWwgACQAAAIxKEwAKAAAAiK4NAAsAAACtYgkADAAAAIb3AwANAAAAM10CAA4AAAA/2gIADwAAAETWDQAQAAAA4hMEABEAAACBjwgAEgAAALgHBAATAAAA1S8CABQAAACiNAMAFQAAAMpGGgAWAAAAokIVABcAAAAJxQgAGAAAAKgMCQAZAAAA0aMJABoAAACJKAgAGwAAAC25BwAcAAAA3tALAB0AAAB8kQQAHgAAAHlVFAAfAAAAMDgnACAAAAAesRcAIQAAACRXHQAiAAAA0hYLACMAAADS5iYAJAAAAE39AwAlAAAAZkIAACYAAACl/BQAJwAAAM5+BAAoAAAAg6sTACkAAABKpAsAKgAAANTHFgArAAAAOwgLACwAAAA1EAkALQAAAPnrGwAuAAAAXdgyAC8AAAAVQAoA.AgAF");

    assertThat(valid).isTrue();
  }

  @Test
  void failsOnEmptySolution() {

    verifier =
        FriendlyCaptchaVerifier.builder()
            .version(FriendlyCaptchaVersion.V1)
            .verificationEndpoint(LOCALHOST)
            .apiKey(VALID_API_KEY)
            .socketTimeout(Duration.ofMinutes(10))
            .build();

    assertThatThrownBy(() -> whenValidatesSolution(""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Solution must not be null or empty");
  }

  @Test
  void failsOnInvalidApiKey() {

    verifier =
        FriendlyCaptchaVerifier.builder()
            .version(FriendlyCaptchaVersion.V1)
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
        FriendlyCaptchaVerifier.builder()
            .version(FriendlyCaptchaVersion.V1)
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
        FriendlyCaptchaVerifier.builder()
            .version(FriendlyCaptchaVersion.V1)
            .verificationEndpoint(LOCALHOST)
            .apiKey(VALID_API_KEY)
            .build();

    assertThatThrownBy(() -> whenValidatesSolution("test"))
        .isInstanceOf(FriendlyCaptchaException.class);
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
                        "{\"success\":false,\"errors\":[\"secret_invalid\"],\"details\":\"Provided secret API key invalid\"}")));

    verifier =
        FriendlyCaptchaVerifier.builder()
            .version(FriendlyCaptchaVersion.V1)
            .verificationEndpoint(LOCALHOST)
            .apiKey(VALID_API_KEY)
            .build();

    assertThatThrownBy(() -> whenValidatesSolution("test"))
        .isInstanceOf(FriendlyCaptchaException.class)
        .hasMessage("Provided secret API key invalid")
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SECRET_INVALID);
  }

  private void whenValidatesSolution(String solution) {
    valid = verifier.verify(solution);
  }
}
