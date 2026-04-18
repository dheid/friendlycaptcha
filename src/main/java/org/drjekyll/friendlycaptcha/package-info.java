/**
 * Friendly Captcha verification client for JVM applications.
 *
 * <p>The main entry point is {@link org.drjekyll.friendlycaptcha.FriendlyCaptchaVerifier}, which
 * verifies Friendly Captcha puzzle solutions against the Friendly Captcha API (v1 or v2).
 *
 * <p>Use {@link org.drjekyll.friendlycaptcha.FriendlyCaptchaVerifier#builder()} to configure and
 * build a verifier instance, then call
 * {@link org.drjekyll.friendlycaptcha.FriendlyCaptchaVerifier#verify(String)} for synchronous
 * verification or
 * {@link org.drjekyll.friendlycaptcha.FriendlyCaptchaVerifier#verifyAsync(String)} for
 * non-blocking verification via {@link java.util.concurrent.CompletableFuture}.
 *
 * <p>API errors are reported as {@link org.drjekyll.friendlycaptcha.FriendlyCaptchaException},
 * which exposes the HTTP status code and a machine-readable
 * {@link org.drjekyll.friendlycaptcha.ErrorCode} for fine-grained error handling.
 */
package org.drjekyll.friendlycaptcha;
