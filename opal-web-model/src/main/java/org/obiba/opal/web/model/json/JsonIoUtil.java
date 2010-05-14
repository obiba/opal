package org.obiba.opal.web.model.json;

import java.io.IOException;
import java.util.Collection;

import com.google.protobuf.JsonFormat;
import com.google.protobuf.Message;

/**
 * Utility class that provides a simple way of writing collections of messages as a JSON array. This method will
 * delegate the Message writing to {@code JsonFormat}.
 */
public final class JsonIoUtil {
  private JsonIoUtil() {
  }

  public static void printCollection(final Collection<Message> messages, final Appendable appendable) throws IOException {
    if(messages == null) throw new IllegalArgumentException("messages cannot be null");
    if(appendable == null) throw new IllegalArgumentException("messages cannot be null");

    // Start the Array
    appendable.append('[');
    boolean first = true;
    for(Message ml : messages) {
      // If this isn't the first item, prepend with a comma
      if(first == false) appendable.append(',');
      first = false;

      JsonFormat.print(ml, appendable);
    }
    // Close the Array
    appendable.append(']');
  }
}
