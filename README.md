# Friendly Captcha Verification API Client

[![Maven Central](https://img.shields.io/maven-central/v/org.drjekyll/friendlycaptcha.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22org.drjekyll%22%20AND%20a:%22friendlycaptcha%22)
[![Java CI with Maven](https://github.com/dheid/friendlycaptcha/actions/workflows/build.yml/badge.svg)](https://github.com/dheid/friendlycaptcha/actions/workflows/build.yml)
[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/W7W3EER56)

This client library allows JVM-based applications to verify [Friendly Captcha](https://www.friendlycaptcha.com) puzzle solutions. It wraps the necessary
call and interprets the result.

- Easy to use (see example below)
- Requires Java 17 or later
- Compatible with JVM-based applications (Java, Groovy, Kotlin, Scala, Clojure)
- Supports both Friendly Captcha API v1 and v2
- Synchronous and asynchronous verification
- Uses the built-in Java HTTP client — no extra HTTP library dependency
- Only two runtime dependencies:
  * [Jackson](https://github.com/FasterXML/jackson) for JSON parsing
  * [SLF4J](https://www.slf4j.org) for logging (if verbose mode is enabled)

## API Documentation and Reports

* [Javadoc](https://dheid.github.io/friendlycaptcha/apidocs)
* [Test Coverage Report](https://dheid.github.io/friendlycaptcha/coverage)

## Comparison with the official SDK

The official [FriendlyCaptcha/friendly-captcha-jvm](https://github.com/FriendlyCaptcha/friendly-captcha-jvm)
SDK is the Friendly Captcha team's own library. It even recommends this library for API v1 support.
Here is how the two compare:

|             Feature              |              **this library**               |          friendly-captcha-jvm           |
|----------------------------------|---------------------------------------------|-----------------------------------------|
| API v1 support                   | Yes                                         | No (v2 only)                            |
| API v2 support                   | Yes                                         | Yes                                     |
| Proxy support (host, port, auth) | Yes                                         | No                                      |
| Connect / request timeout        | Yes                                         | No                                      |
| Regional / custom endpoint       | Yes                                         | No                                      |
| Custom `User-Agent`              | Yes                                         | No                                      |
| Verbose SLF4J logging            | Yes                                         | No                                      |
| HTTP client                      | Built-in Java `HttpClient`                  | Separate HTTP library                   |
| API style                        | Synchronous and async (`CompletableFuture`) | Asynchronous only (`CompletableFuture`) |
| Risk intelligence retrieval      | No                                          | Yes                                     |
| Minimum Java version             | 17                                          | 8                                       |
| License                          | LGPL                                        | MIT                                     |

## Usage

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

### Asynchronous verification

`verifyAsync(solution)` returns a `CompletableFuture<Boolean>` and uses the non-blocking
`HttpClient.sendAsync` under the hood — no thread is blocked while the request is in flight.

```java
friendlyCaptchaVerifier.verifyAsync(solution)
    .thenAccept(success -> {
      if (success) {
        // continue
      } else {
        // solution invalid, expired, or already used — reject the submission
      }
    })
    .exceptionally(ex -> {
      Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
      if (cause instanceof FriendlyCaptchaException fce && fce.getStatusCode() != null
          && fce.getStatusCode() == 503) {
        // API temporarily unavailable — fail open
      } else {
        // permanent error — log and handle
      }
      return null;
    });
```

The future completes exceptionally with a `CompletionException` whose cause is always a
`FriendlyCaptchaException` — network failures are wrapped in one just like the synchronous
`verify` method. The same `getStatusCode()` / `getErrorCode()` introspection described below
applies to the unwrapped cause.

### Handling FriendlyCaptchaException

`FriendlyCaptchaException` exposes two optional details:

- `getStatusCode()` — the HTTP status code returned by the API, or `null` for non-HTTP failures
  (network errors, unreadable responses, invalid configuration).
- `getErrorCode()` — the machine-readable `ErrorCode` from the response body, or `null` when the
  API did not include one.

**Retrying on 503 (service unavailable)**

A 503 response means the Friendly Captcha API was temporarily unavailable. In this case it is safe
to fail open (accept the submission) rather than blocking the user, and schedule a retry later:

```java
try {
  boolean success = friendlyCaptchaVerifier.verify(solution);
  if (!success) {
    // reject
  }
} catch (FriendlyCaptchaException e) {
  if (e.getStatusCode() != null && e.getStatusCode() == 503) {
    // API temporarily unavailable — fail open and retry later
    log.warn("Friendly Captcha API unavailable (503), failing open", e);
  } else {
    // Permanent error — check credentials and request format
    throw e;
  }
}
```

**Evaluating the error code for troubleshooting**

When `getErrorCode()` is non-null you can branch on the specific `ErrorCode` constant for
fine-grained error handling or logging:

```java
} catch (FriendlyCaptchaException e) {
  ErrorCode code = e.getErrorCode();
  if (code == ErrorCode.AUTH_INVALID || code == ErrorCode.SECRET_INVALID) {
    log.error("API key is invalid — check your Friendly Captcha account settings");
  } else if (code == ErrorCode.SITEKEY_INVALID) {
    log.error("Sitekey mismatch — ensure the widget sitekey matches the verifier");
  } else if (e.getStatusCode() != null && e.getStatusCode() == 503) {
    log.warn("Friendly Captcha API temporarily unavailable (503), failing open");
  } else {
    log.error("Captcha verification failed: {} (HTTP {})", code, e.getStatusCode(), e);
  }
}
```

The full set of error codes is documented in the `ErrorCode` enum Javadoc and in the
[Friendly Captcha API reference](https://developer.friendlycaptcha.com/).

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

## Verifier Parameters

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
| `.userAgent(...)`            | Custom `User-Agent` header value sent with every request. Defaults to `FriendlyCaptchaJavaClient`.                                                                                                                                                                |
| `.verbose(true)`             | Logs endpoint and response details at INFO level via SLF4J.                                                                                                                                                                                                       |

## Development

To build and locally install the library and run the tests, just call

```shell
mvn install
```

## Contributing

Please read [the contribution document](CONTRIBUTING.md) for details on our code of conduct, and the
process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see
the [tags on this repository](https://github.com/dheid/friendlycaptcha/tags).

## License

This project is licensed under the LGPL License - see the [license](LICENSE) file for details.

## Release Notes

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
- New `.userAgent(...)` builder parameter to override the default `User-Agent` header
- Added `verifyAsync(solution)` returning `CompletableFuture<Boolean>` for non-blocking verification

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

