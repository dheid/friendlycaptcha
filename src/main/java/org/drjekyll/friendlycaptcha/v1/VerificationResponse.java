package org.drjekyll.friendlycaptcha.v1;

import java.util.Collection;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.drjekyll.friendlycaptcha.ErrorCode;

@Value
@Builder
@Jacksonized
class VerificationResponse {

  boolean success;

  String details;

  Collection<ErrorCode> errors;
}
