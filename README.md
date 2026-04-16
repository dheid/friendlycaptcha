# :robot: Friendly Captcha Verification API Client

[![Maven Central](https://img.shields.io/maven-central/v/org.drjekyll/friendlycaptcha.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22org.drjekyll%22%20AND%20a:%22friendlycaptcha%22)
[![Java CI with Maven](https://github.com/dheid/friendlycaptcha/actions/workflows/build.yml/badge.svg)](https://github.com/dheid/friendlycaptcha/actions/workflows/build.yml)
[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/W7W3EER56)

This client library allows JVM-based applications to verify [Friendly Captcha](https://www.friendlycaptcha.com) puzzle solutions. It wraps the necessary
call and interprets the result.

* Easy to use (see example below)
* Requires Java 17 or later
* Compatible with JVM-based applications (Java, Groovy, Kotlin, Scala, Clojure)
* Supports both Friendly Captcha API v1 and v2
* Uses the built-in Java HTTP client — no extra HTTP library dependency
* Only two dependencies: Jackson and SLF4J

## :wrench: Usage

Include the dependency using Maven

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

### API v2 (recommended)

Friendly Captcha API v2 is the current version. The API key is sent as a request header and the
endpoint is `https://global.frcapi.com/api/v2/captcha/siteverify`.

```java
import org.drjekyll.friendlycaptcha.FriendlyCaptchaVerifier;
import org.drjekyll.friendlycaptcha.FriendlyCaptchaVerifierV2;

public class FriendlyCaptchaV2Example {

  private final FriendlyCaptchaVerifier friendlyCaptchaVerifier = FriendlyCaptchaVerifierV2
    .builder()
    .apiKey("YOUR_API_KEY")
    .sitekey("AN_OPTIONAL_SITE_KEY")
    .build();

  public void checkSolution(String solution) {

    boolean success = friendlyCaptchaVerifier.verify(solution);
    if (success) {
      // continue
    } else {
      // stop processing
    }

  }

}
```

Or Kotlin:

```kotlin
import org.drjekyll.friendlycaptcha.FriendlyCaptchaVerifier
import org.drjekyll.friendlycaptcha.FriendlyCaptchaVerifierV2

class FriendlyCaptchaV2Example {
  private val friendlyCaptchaVerifier: FriendlyCaptchaVerifier = FriendlyCaptchaVerifierV2
    .builder()
    .apiKey("YOUR_API_KEY")
    .sitekey("AN_OPTIONAL_SITE_KEY")
    .build()

  fun checkSolution(solution: String?) {
    val success: Boolean = friendlyCaptchaVerifier.verify(solution)
    if (success) {
      // continue
    } else {
      // stop processing
    }
  }
}
```

### API v1 (legacy)

Friendly Captcha API v1 is the legacy version. The API key and solution are sent as form fields to
`https://api.friendlycaptcha.com/api/v1/siteverify`.

```java
import org.drjekyll.friendlycaptcha.FriendlyCaptchaVerifier;
import org.drjekyll.friendlycaptcha.FriendlyCaptchaVerifierV1;

public class FriendlyCaptchaV1Example {

  private final FriendlyCaptchaVerifier friendlyCaptchaVerifier = FriendlyCaptchaVerifierV1
    .builder()
    .apiKey("YOUR_API_KEY")
    .sitekey("AN_OPTIONAL_SITE_KEY")
    .build();

  public void checkSolution(String solution) {

    boolean success = friendlyCaptchaVerifier.verify(solution);
    if (success) {
      // continue
    } else {
      // stop processing
    }

  }

}
```

Or Kotlin:

```kotlin
import org.drjekyll.friendlycaptcha.FriendlyCaptchaVerifier
import org.drjekyll.friendlycaptcha.FriendlyCaptchaVerifierV1

class FriendlyCaptchaV1Example {
  private val friendlyCaptchaVerifier: FriendlyCaptchaVerifier = FriendlyCaptchaVerifierV1
    .builder()
    .apiKey("YOUR_API_KEY")
    .sitekey("AN_OPTIONAL_SITE_KEY")
    .build()

  fun checkSolution(solution: String?) {
    val success: Boolean = friendlyCaptchaVerifier.verify(solution)
    if (success) {
      // continue
    } else {
      // stop processing
    }
  }
}
```

On a non-successful response, `verify` throws a `FriendlyCaptchaException` containing either the response details or a description of the error.

### Migration from 2.x

#### Java version requirement

Version 3.0.0 requires **Java 17 or later**. If your project still targets Java 8, stay on
the 2.x release line.

#### Replace deprecated builder

Code using the old `FriendlyCaptchaVerifier.builder()` still compiles and behaves identically
(v1 API), but is now deprecated. Replace it with `FriendlyCaptchaVerifierV1.builder()`:

```java
// Before (deprecated)
FriendlyCaptchaVerifier verifier = FriendlyCaptchaVerifier.builder()
    .apiKey("YOUR_API_KEY")
    .build();

// After
FriendlyCaptchaVerifier verifier = FriendlyCaptchaVerifierV1.builder()
    .apiKey("YOUR_API_KEY")
    .build();
```

## :gear: Verifier Parameters

The Friendly Captcha Verifier currently supports the following builder methods:

Both `FriendlyCaptchaVerifierV1` and `FriendlyCaptchaVerifierV2` share the following builder
methods:

* `.apiKey(...)` An API key that proves it's you, create one on the Friendly Captcha website.
  For v2, the key is sent as the `X-API-Key` request header. For v1, it is sent as the `secret`
  form field.
* `.objectMapper(...)` If you would like to use an existing or custom object mapper
* `.verificationEndpoint(...)` An `URI` object that can point to another verification endpoint (for
  example a regional endpoint). If not set, defaults to
  `https://global.frcapi.com/api/v2/captcha/siteverify` for `FriendlyCaptchaVerifierV2` and
  `https://api.friendlycaptcha.com/api/v1/siteverify` for `FriendlyCaptchaVerifierV1`.
* `.connectTimeout(...)` allows you to change the default connection timeout of 10 seconds. 0 is
  interpreted as infinite, null uses the system default
* `.socketTimeout(...)` allows you to change the default request timeout of 30 seconds, which
  covers the entire request from sending to receiving the full response. A `null` value means no
  timeout is applied.
* `.sitekey(...)` is an optional sitekey that you want to make sure the puzzle was generated from.
* `.proxyHost(...)` The hostname or IP address of an optional HTTP proxy. `proxyPort` must be
  configured as well
* `.proxyPort(...)` The port of an HTTP proxy. `proxyHost` must be configured as well.
* `.proxyUserName(...)` If the HTTP proxy requires a user name for basic authentication, it can be
  configured with this method. Proxy host, port and password must also be set.
* `.proxyPassword(...)` The corresponding password for the basic auth proxy user. The proxy host,
  port and user name must be set as well.
* `.verbose(...)` logs detailed information using INFO level

## :factory_worker: Development

To build and locally install the library and run the tests, just call

        mvn install

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

* **Requires Java 17** — dropped support for Java 8
* Replaced `HttpURLConnection` with the built-in Java `HttpClient` (`java.net.http`) — no
  third-party HTTP library required
* `socketTimeout` now covers the entire request duration (connect + send + receive) instead of
  the per-read socket timeout
* Split `FriendlyCaptchaVerifier` into an abstract base class and two concrete implementations:
  `FriendlyCaptchaVerifierV1` (legacy API) and `FriendlyCaptchaVerifierV2` (current API)
* Added support for Friendly Captcha API v2: sends the API key as the `X-API-Key` header, uses
  the `response` body parameter, and parses the v2 response format
* Deprecated `FriendlyCaptchaVerifier.builder()` — use `FriendlyCaptchaVerifierV1.builder()` or
  `FriendlyCaptchaVerifierV2.builder()` instead
* Fixed sitekey not being URL-encoded in the POST body
* Fixed potential NullPointerException when the API returns an error response without a body

### 2.0.10 / 2.0.11

Dependency updates

### 2.0.2 -- 2.0.9

Dependency updates

### 2.0.1

Got rid of HTTP client dependency. Apache HTTP Client is no longer needed.

### 1.2.1 / 1.2.2

* Update dependencies

### 1.2.0

* Add verbose logging

### 1.1.0

* Add proxy authentication

### 1.0.0

* Initial version

