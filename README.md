# :robot: Friendly Captcha Verification API Client

[![Maven Central](https://img.shields.io/maven-central/v/org.drjekyll/friendlycaptcha.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22org.drjekyll%22%20AND%20a:%22friendlycaptcha%22)
[![Java CI with Maven](https://github.com/dheid/friendlycaptcha/actions/workflows/build.yml/badge.svg)](https://github.com/dheid/friendlycaptcha/actions/workflows/build.yml)
[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/W7W3EER56)

This client library allows JVM-based applications to verify [Friendly Captcha](https://www.friendlycaptcha.com) puzzle solutions. It wraps the necessary
call and interprets the result.

- Easy to use (see example below)
- Requires Java 17 or later
- Compatible with JVM-based applications (Java, Groovy, Kotlin, Scala, Clojure)
- Supports both Friendly Captcha API v1 and v2
- Uses the built-in Java HTTP client — no extra HTTP library dependency
- Only two runtime dependencies: [Jackson 3](https://github.com/FasterXML/jackson) (`tools.jackson.core:jackson-databind`) and SLF4J

## :wrench: Usage

Include the dependency using Maven:

```xml
<dependency>
  <groupId>org.drjekyll</groupId>
  <artifactId>friendlycaptcha</artifactId>
  <version>3.0.0</version>
</dependency>
```

or Gradle with Groovy DSL:

```groovy
implementation 'org.drjekyll:friendlycaptcha:3.0.0'
```

or Gradle with Kotlin DSL:

```kotlin
implementation("org.drjekyll:friendlycaptcha:3.0.0")
```

> **Jackson 3 note:** This library depends on Jackson 3 (`tools.jackson.core:jackson-databind`).
> If your project currently uses Jackson 2 (`com.fasterxml.jackson.core`), you will need to
> [migrate to Jackson 3](https://github.com/FasterXML/jackson/blob/main/jackson3/MIGRATING_TO_JACKSON_3.md)
> or manage both versions on the classpath.

### API v2 (recommended)

Friendly Captcha API v2 is the current recommended version. The API key is sent as the
`X-API-Key` request header and the solution as the `response` body parameter to
`https://global.frcapi.com/api/v2/captcha/siteverify`.

```java
import org.drjekyll.friendlycaptcha.FriendlyCaptchaException;
import org.drjekyll.friendlycaptcha.FriendlyCaptchaVerifier;
import org.drjekyll.friendlycaptcha.FriendlyCaptchaVersion;

public class FriendlyCaptchaV2Example {

  private final FriendlyCaptchaVerifier friendlyCaptchaVerifier = FriendlyCaptchaVerifier
    .builder()
    .version(FriendlyCaptchaVersion.V2)
    .apiKey("YOUR_API_KEY")
    .sitekey("AN_OPTIONAL_SITE_KEY")
    .build();

  public void checkSolution(String solution) {
    try {
      boolean success = friendlyCaptchaVerifier.verify(solution);
      if (success) {
        // continue
      } else {
        // solution invalid, expired, or already used — reject the submission
      }
    } catch (FriendlyCaptchaException e) {
      // API or network error — log and decide whether to fail open or closed
    }
  }

}
```

Or Kotlin:

```kotlin
import org.drjekyll.friendlycaptcha.FriendlyCaptchaException
import org.drjekyll.friendlycaptcha.FriendlyCaptchaVerifier
import org.drjekyll.friendlycaptcha.FriendlyCaptchaVersion

class FriendlyCaptchaV2Example {
  private val friendlyCaptchaVerifier: FriendlyCaptchaVerifier = FriendlyCaptchaVerifier
    .builder()
    .version(FriendlyCaptchaVersion.V2)
    .apiKey("YOUR_API_KEY")
    .sitekey("AN_OPTIONAL_SITE_KEY")
    .build()

  fun checkSolution(solution: String?) {
    try {
      val success: Boolean = friendlyCaptchaVerifier.verify(solution)
      if (success) {
        // continue
      } else {
        // solution invalid, expired, or already used — reject the submission
      }
    } catch (e: FriendlyCaptchaException) {
      // API or network error — log and decide whether to fail open or closed
    }
  }
}
```

### API v1 (legacy, default)

Friendly Captcha API v1 is the default when no `.version(...)` is set. The API key is sent as
the `secret` form field and the solution as the `solution` form field to
`https://api.friendlycaptcha.com/api/v1/siteverify`.

```java
import org.drjekyll.friendlycaptcha.FriendlyCaptchaException;
import org.drjekyll.friendlycaptcha.FriendlyCaptchaVerifier;

public class FriendlyCaptchaExample {

  private final FriendlyCaptchaVerifier friendlyCaptchaVerifier = FriendlyCaptchaVerifier
    .builder()
    .apiKey("YOUR_API_KEY")
    .sitekey("AN_OPTIONAL_SITE_KEY")
    .build();

  public void checkSolution(String solution) {
    try {
      boolean success = friendlyCaptchaVerifier.verify(solution);
      if (success) {
        // continue
      } else {
        // solution invalid, expired, or already used — reject the submission
      }
    } catch (FriendlyCaptchaException e) {
      // API or network error — log and decide whether to fail open or closed
    }
  }

}
```

Or Kotlin:

```kotlin
import org.drjekyll.friendlycaptcha.FriendlyCaptchaException
import org.drjekyll.friendlycaptcha.FriendlyCaptchaVerifier

class FriendlyCaptchaExample {
  private val friendlyCaptchaVerifier: FriendlyCaptchaVerifier = FriendlyCaptchaVerifier
    .builder()
    .apiKey("YOUR_API_KEY")
    .sitekey("AN_OPTIONAL_SITE_KEY")
    .build()

  fun checkSolution(solution: String?) {
    try {
      val success: Boolean = friendlyCaptchaVerifier.verify(solution)
      if (success) {
        // continue
      } else {
        // solution invalid, expired, or already used — reject the submission
      }
    } catch (e: FriendlyCaptchaException) {
      // API or network error — log and decide whether to fail open or closed
    }
  }
}
```

### Return values and exceptions

`verify(solution)` has three possible outcomes, regardless of whether you use v1 or v2:

|              Outcome              |                                                   When                                                   |
|-----------------------------------|----------------------------------------------------------------------------------------------------------|
| Returns `true`                    | The solution is valid and was accepted                                                                   |
| Returns `false`                   | The solution is invalid, expired, or already used                                                        |
| Throws `FriendlyCaptchaException` | The API rejected the request itself (bad API key, malformed request, network error, unreadable response) |

`verify` also throws `IllegalArgumentException` if the solution or API key is null or empty.

### Regional endpoints (v2)

The v2 API offers regional endpoints. Pass a custom URI via `.verificationEndpoint(...)`:

```java
FriendlyCaptchaVerifier verifier = FriendlyCaptchaVerifier.builder()
    .version(FriendlyCaptchaVersion.V2)
    .apiKey("YOUR_API_KEY")
    .verificationEndpoint(URI.create("https://eu.frcapi.com/api/v2/captcha/siteverify"))
    .build();
```

### Migration from 2.x

#### Java version requirement

Version 3.0.0 requires **Java 17 or later**. If your project still targets Java 8, stay on
the 2.x release line.

#### Jackson dependency

Version 3.0.0 upgraded the Jackson dependency from Jackson 2 (`com.fasterxml.jackson.core`) to
Jackson 3 (`tools.jackson.core`). If your project still uses Jackson 2, you will need to either
migrate alongside this library or continue on the 2.x release line.

## :gear: Verifier Parameters

`FriendlyCaptchaVerifier.builder()` supports the following methods:

|          Parameter           |                                                                                                                            Description                                                                                                                            |
|------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `.apiKey(...)`               | **Required.** The API key from your Friendly Captcha account.                                                                                                                                                                                                     |
| `.version(...)`              | `FriendlyCaptchaVersion.V1` (default) or `FriendlyCaptchaVersion.V2` (recommended). For v1, the API key is sent as the `secret` form field. For v2, it is sent as the `X-API-Key` request header.                                                                 |
| `.sitekey(...)`              | Optional sitekey to verify that the puzzle was generated for your site.                                                                                                                                                                                           |
| `.verificationEndpoint(...)` | Custom verification endpoint URI. Defaults to `https://api.friendlycaptcha.com/api/v1/siteverify` for v1 and `https://global.frcapi.com/api/v2/captcha/siteverify` for v2. Use `https://eu.frcapi.com/api/v2/captcha/siteverify` for EU-only data residency (v2). |
| `.connectTimeout(...)`       | Connection establishment timeout (`Duration`). `null` uses the system default, `Duration.ZERO` means infinite.                                                                                                                                                    |
| `.socketTimeout(...)`        | Total request timeout (`Duration`) covering the entire request from sending to receiving the full response. `null` means no timeout.                                                                                                                              |
| `.objectMapper(...)`         | Custom Jackson 3 `ObjectMapper` instance. If not set, a default `ObjectMapper` is used.                                                                                                                                                                           |
| `.proxyHost(...)`            | Hostname or IP address of an HTTP proxy. `proxyPort` must also be set.                                                                                                                                                                                            |
| `.proxyPort(...)`            | Port of an HTTP proxy. `proxyHost` must also be set.                                                                                                                                                                                                              |
| `.proxyUserName(...)`        | Username for HTTP proxy basic authentication. `proxyHost`, `proxyPort`, and `proxyPassword` must also be set.                                                                                                                                                     |
| `.proxyPassword(...)`        | Password for HTTP proxy basic authentication. `proxyHost`, `proxyPort`, and `proxyUserName` must also be set.                                                                                                                                                     |
| `.verbose(true)`             | Logs endpoint and response details at INFO level via SLF4J.                                                                                                                                                                                                       |

## :factory_worker: Development

To build and locally install the library and run the tests, just call

```shell
mvn install
```

## :handshake: Contributing

Please read [the contribution document](CONTRIBUTING.md) for details on our code of conduct, and the
process for submitting pull requests to us.

## :notebook: Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see
the [tags on this repository](https://github.com/dheid/friendlycaptcha/tags).

## :scroll: License

This project is licensed under the LGPL License - see the [license](LICENSE) file for details.

## :loudspeaker: Release Notes

### 3.0.0

- **Requires Java 17** — dropped support for Java 8
- **Upgraded to Jackson 3** (`tools.jackson.core:jackson-databind:3.x`) — Jackson 2 is no longer
  a transitive dependency
- Replaced `HttpURLConnection` with the built-in Java `HttpClient` (`java.net.http`) — no
  third-party HTTP library required
- `socketTimeout` now covers the entire request duration (connect + send + receive) instead of
  the per-read socket timeout
- Single `FriendlyCaptchaVerifier.builder()` entry point — select the API version via
  `.version(FriendlyCaptchaVersion.V1)` (default) or `.version(FriendlyCaptchaVersion.V2)`
- Added support for Friendly Captcha API v2: sends the API key as the `X-API-Key` header, uses
  the `response` body parameter, and parses the v2 response format
- Fixed sitekey not being URL-encoded in the POST body
- Internal refactoring: v1 and v2 implementations moved to `org.drjekyll.friendlycaptcha.v1`
  and `org.drjekyll.friendlycaptcha.v2` sub-packages

### 2.0.10 / 2.0.11

Dependency updates

### 2.0.2 -- 2.0.9

Dependency updates

### 2.0.1

Got rid of HTTP client dependency. Apache HTTP Client is no longer needed.

### 1.2.1 / 1.2.2

- Update dependencies

### 1.2.0

- Add verbose logging

### 1.1.0

- Add proxy authentication

### 1.0.0

- Initial version

