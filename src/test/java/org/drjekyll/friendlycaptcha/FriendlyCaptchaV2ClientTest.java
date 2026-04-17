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
class FriendlyCaptchaV2ClientTest {

  private static final String VALID_API_KEY =
      "B191X90HRE6PA37HDSUIMXS6L46HQGL1A5PGJBFQ12VCV52GTI4HJA2CGI";

  private static final String SITEKEY = "NQTVT3JKLX8WX1VQ";

  private static final URI LOCALHOST = URI.create("http://localhost:8080/");

  private FriendlyCaptchaVerifier verifier;

  private boolean valid;

  @Test
  void validSolutionIsValid() {

    stubFor(
        post("/")
            .withHeader("X-API-Key", equalTo(VALID_API_KEY))
            .withRequestBody(equalTo("response=valid-solution&sitekey=" + SITEKEY))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{\"success\":true,\"data\":{\"event_id\":\"abc123\",\"challenge\":{\"timestamp\":\"2024-01-01T00:00:00Z\",\"origin\":\"https://example.com\"},\"risk_intelligence\":null}}")));

    verifier =
        FriendlyCaptchaVerifier.builder()
            .version(FriendlyCaptchaVersion.V2)
            .verificationEndpoint(LOCALHOST)
            .apiKey(VALID_API_KEY)
            .sitekey(SITEKEY)
            .build();

    whenValidatesSolution("valid-solution");

    assertThat(valid).isTrue();
  }

  @Test
  void invalidSolutionIsInvalid() {

    stubFor(
        post("/")
            .withHeader("X-API-Key", equalTo(VALID_API_KEY))
            .withRequestBody(equalTo("response=invalid-solution"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{\"success\":false,\"error\":{\"error_code\":\"response_invalid\",\"detail\":\"The response was invalid\"}}")));

    verifier =
        FriendlyCaptchaVerifier.builder()
            .version(FriendlyCaptchaVersion.V2)
            .verificationEndpoint(LOCALHOST)
            .apiKey(VALID_API_KEY)
            .build();

    whenValidatesSolution("invalid-solution");

    assertThat(valid).isFalse();
  }

  @Test
  void timeoutSolutionIsInvalid() {

    stubFor(
        post("/")
            .withHeader("X-API-Key", equalTo(VALID_API_KEY))
            .withRequestBody(equalTo("response=expired-solution"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{\"success\":false,\"error\":{\"error_code\":\"response_timeout\",\"detail\":\"The response has expired\"}}")));

    verifier =
        FriendlyCaptchaVerifier.builder()
            .version(FriendlyCaptchaVersion.V2)
            .verificationEndpoint(LOCALHOST)
            .apiKey(VALID_API_KEY)
            .verbose(true)
            .build();

    whenValidatesSolution("expired-solution");

    assertThat(valid).isFalse();
  }

  @Test
  void failsOnInvalidApiKey() {

    stubFor(
        post("/")
            .withHeader("X-API-Key", equalTo("invalid-key"))
            .willReturn(
                aResponse()
                    .withStatus(401)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{\"success\":false,\"error\":{\"error_code\":\"auth_invalid\",\"detail\":\"[40202]\"}}")));

    verifier =
        FriendlyCaptchaVerifier.builder()
            .version(FriendlyCaptchaVersion.V2)
            .verificationEndpoint(LOCALHOST)
            .apiKey("invalid-key")
            .socketTimeout(Duration.ofMinutes(10L))
            .build();

    assertThatThrownBy(() -> whenValidatesSolution("test"))
        .isInstanceOf(FriendlyCaptchaException.class)
        .hasMessage("The provided API key was invalid");
  }

  @Test
  void apiKeyIsNotIncludedInBody() {

    stubFor(
        post("/")
            .withHeader("X-API-Key", equalTo(VALID_API_KEY))
            .withRequestBody(equalTo("response=test"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"success\":true}")));

    verifier =
        FriendlyCaptchaVerifier.builder()
            .version(FriendlyCaptchaVersion.V2)
            .verificationEndpoint(LOCALHOST)
            .apiKey(VALID_API_KEY)
            .build();

    whenValidatesSolution("test");

    assertThat(valid).isTrue();
  }

  @Test
  void handlesErrorResponseWithoutBody() {

    stubFor(post("/").willReturn(aResponse().withStatus(500)));

    verifier =
        FriendlyCaptchaVerifier.builder()
            .version(FriendlyCaptchaVersion.V2)
            .verificationEndpoint(LOCALHOST)
            .apiKey(VALID_API_KEY)
            .build();

    assertThatThrownBy(() -> whenValidatesSolution("test"))
        .isInstanceOf(FriendlyCaptchaException.class);
  }

  @Test
  void sitekeyWithSpecialCharactersIsUrlEncoded() {

    stubFor(
        post("/")
            .withHeader("X-API-Key", equalTo(VALID_API_KEY))
            .withRequestBody(equalTo("response=test&sitekey=NQT%2BVT3%2FJKLX"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"success\":true}")));

    verifier =
        FriendlyCaptchaVerifier.builder()
            .version(FriendlyCaptchaVersion.V2)
            .verificationEndpoint(LOCALHOST)
            .apiKey(VALID_API_KEY)
            .sitekey("NQT+VT3/JKLX")
            .build();

    whenValidatesSolution("test");

    assertThat(valid).isTrue();
  }

  @Test
  void includesErrorCode() {

    stubFor(
        post("/")
            .withHeader("X-API-Key", equalTo(VALID_API_KEY))
            .willReturn(
                aResponse()
                    .withStatus(400)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"success\":false,\"error\":{\"error_code\":\"bad_request\"}}")));

    verifier =
        FriendlyCaptchaVerifier.builder()
            .version(FriendlyCaptchaVersion.V2)
            .verificationEndpoint(LOCALHOST)
            .apiKey(VALID_API_KEY)
            .build();

    assertThatThrownBy(() -> whenValidatesSolution("test"))
        .isInstanceOf(FriendlyCaptchaException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BAD_REQUEST);
    ;
  }

  private void whenValidatesSolution(String solution) {
    valid = verifier.verify(solution);
  }
}
