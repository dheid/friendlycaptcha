package org.drjekyll.friendlycaptcha;

import java.util.Collection;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class VerificationResponse {

  private boolean success;

  private String details;

  private Collection<VerificationError> errors;

}
