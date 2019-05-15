package org.obiba.opal.web.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConflictingRequestException extends RuntimeException {

  private final List<String> messageArgs;

  public ConflictingRequestException(String message, String... messageArgs) {
    super(message);

    this.messageArgs = new ArrayList<>();

    if(messageArgs != null) {
      Collections.addAll(this.messageArgs, messageArgs);
    }
  }

  public List<String> getMessageArgs() {
    return Collections.unmodifiableList(messageArgs);
  }
}
