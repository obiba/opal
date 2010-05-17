package org.obiba.opal.web.model.json;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collection;

import com.google.protobuf.JsonFormat;
import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

/**
 * Utility class that provides a simple way of writing collections of messages as a JSON array. This method will
 * delegate the Message writing to {@code JsonFormat}.
 */
public final class JsonIoUtil {

  private static final char JS_ARRAY_OPEN = '[';

  private static final char JS_ARRAY_SEP = ',';

  private static final char JS_ARRAY_CLOSE = ']';

  private JsonIoUtil() {
  }

  public static <T extends Message> ArrayList<T> mergeCollection(final Builder builder, final Readable readable) throws IOException {
    if(builder == null) throw new IllegalArgumentException("builder cannot be null");
    if(readable == null) throw new IllegalArgumentException("readable cannot be null");

    CharBuffer cb = CharBuffer.allocate(1);
    // Read the array opening tag '['
    int read = readable.read(cb);
    if(read != 1 && cb.charAt(0) != JS_ARRAY_OPEN) {
      throw new IOException("unexpected state");
    }
    ArrayList<T> messages = new ArrayList<T>();
    do {
      builder.clear();
      JsonFormat.merge(readable, builder);
      messages.add((T) builder.build());
      // read the next char which is either ',' or ']'
      read = readable.read(cb);
    } while(read > 0 && cb.charAt(0) == JS_ARRAY_SEP);

    // Make sure we read the closing character, and not some other strange char
    if(cb.charAt(0) != JS_ARRAY_CLOSE) {
      throw new IOException("unexpected state");
    }
    return messages;

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
