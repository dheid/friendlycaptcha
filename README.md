# :robot: Friendly Captcha Verification API Client

[![Maven Central](https://img.shields.io/maven-central/v/org.drjekyll/friendlycaptcha.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22org.drjekyll%22%20AND%20a:%22friendlycaptcha%22)
[![Java CI with Maven](https://github.com/dheid/friendlycaptcha/actions/workflows/build.yml/badge.svg)](https://github.com/dheid/friendlycaptcha/actions/workflows/build.yml)
[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/W7W3EER56)

This client library allows JVM-based applications to
verify [Friendly Captcha](https://www.friendlycaptcha.com) puzzle solutions. It wraps the necessary
call and interprets the result.

## :wrench: Usage

Include the dependency using Maven

```xml

<dependency>
  <groupId>org.drjekyll</groupId>
  <artifactId>friendlycaptcha</artifactId>
  <version>2.0.7</version>
</dependency>
```

or Gradle with Groovy DSL:

```groovy
implementation 'org.drjekyll:friendlycaptcha:2.0.7'
```

or Gradle with Kotlin DSL:

```kotlin
implementation("org.drjekyll:friendlycaptcha:2.0.7")
```

Run your build tool and you can include the verifier as follows:

```java
import org.drjekyll.friendlycaptcha.FriendlyCaptchaVerifier;

public class FriendlyCaptchaExample {

  private final FriendlyCaptchaVerifier friendlyCaptchaVerifier = FriendlyCaptchaVerifier
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

## :gear: Verifier Parameters

The Friendly Captcha Verifier currently supports the following builder methods:

* `.apiKey(...)` An API key that proves it's you, create one on the Friendly Captcha website
* `.objectMapper(...)` If you would like to use an existing or custom object mapper
* `.verificationEndpoint(...)` An `URI` object that can point to another verification endpoint (for
  example if you would like to use EU hosts). Default
  is: https://api.friendlycaptcha.com/api/v1/siteverify
* `.connectTimeout(...)` allows you to change the default connection timeout of 10 seconds. 0 is
  interpreted as infinite, null uses the system default
* `.socketTimeout(...)` allows you to change the default socket timeout of 10 seconds. 0 is
  interpreted as infinite, null uses the system default
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

### 2.0.2 -- 2.0.7

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
