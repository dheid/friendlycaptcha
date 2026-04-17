package org.drjekyll.friendlycaptcha;

import java.util.Collection;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
class VerificationResponseV1 {

  boolean success;

  String details;

  Collection<ErrorCode> errors;
}
