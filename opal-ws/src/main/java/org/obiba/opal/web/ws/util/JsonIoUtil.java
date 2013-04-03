package org.obiba.opal.web.ws.util;

import java.io.IOException;
import java.util.ArrayList;

import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;
import com.googlecode.protobuf.format.JsonFormat;

/**
 * Utility class that provides a simple way of writing collections of messages as a JSON array. This method will
 * delegate the Message writing to {@code JsonFormat}.
 */
@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
public final class JsonIoUtil {

  private static final char JS_ARRAY_OPEN = '[';

  private static final String JS_ARRAY_OPEN_STR = String.valueOf(JS_ARRAY_OPEN);

  private static final char JS_ARRAY_SEP = ',';

  private static final String JS_ARRAY_SEP_STR = String.valueOf(JS_ARRAY_SEP);

  private static final char JS_ARRAY_CLOSE = ']';

  private static final String JS_ARRAY_CLOSE_STR = String.valueOf(JS_ARRAY_CLOSE);

  private JsonIoUtil() {
  }

  public static <T extends Message> ArrayList<T> mergeCollection(Readable reader, Builder builder) throws IOException {
    return mergeCollection(reader, ExtensionRegistry.getEmptyRegistry(), builder);
  }

  public static <T extends Message> ArrayList<T> mergeCollection(Readable reader, ExtensionRegistry extensionRegistry,
      Builder builder) throws IOException {
    if(reader == null) throw new IllegalArgumentException("reader cannot be null");
    if(extensionRegistry == null) throw new IllegalArgumentException("extensionRegistry cannot be null");
    if(builder == null) throw new IllegalArgumentException("builder cannot be null");

    final ArrayList<T> messages = new ArrayList<T>();
    InnerJsonFormat.mergeCollection(reader, extensionRegistry, builder, new MergeCallback() {

      @Override
      @SuppressWarnings("unchecked")
      public void onMerge(Builder builder) {
        messages.add((T) builder.build());
      }
    });
    return messages;
  }

  public static void printCollection(Iterable<? extends Message> messages, Appendable appendable) throws IOException {
    if(messages == null) throw new IllegalArgumentException("messages cannot be null");
    if(appendable == null) throw new IllegalArgumentException("messages cannot be null");

    // Start the Array
    appendable.append('[');
    boolean first = true;
    for(Message ml : messages) {
      // If this isn't the first item, prepend with a comma
      if(!first) appendable.append(',');
      first = false;

      JsonFormat.print(ml, appendable);
    }
    // Close the Array
    appendable.append(']');
  }

  /**
   * Callback called when a Message instance part of a collection has been read. Ususally, the message instance would be
   * added to a collection.
   */
  private interface MergeCallback {

    /**
     * Called when a Message has been read from a stream of Messages. Note that this method may never be called if the
     * stream contains 0 Messages.
     *
     * @param builder
     */
    void onMerge(Builder builder);
  }

  /**
   * Used to call protected methods on JsonFormat
   */
  private static final class InnerJsonFormat extends JsonFormat {

    static void mergeCollection(Readable reader, ExtensionRegistry extensionRegistry, Builder builder,
        MergeCallback callback) throws IOException {
      CharSequence input = toStringBuilder(reader);
      Tokenizer tokenizer = new Tokenizer(input.subSequence(0, input.length()));
      tokenizer.consume(JS_ARRAY_OPEN_STR);
      // Special case of empty list of messages: "[]"
      if(!tokenizer.tryConsume(JS_ARRAY_CLOSE_STR)) {
        // At least one Message present
        do {
          Builder thisBuilder = builder.clone();
          tokenizer.consume("{"); // Needs to happen when the object starts.
          while(!tokenizer.tryConsume("}")) { // Continue till the object is done
            mergeField(tokenizer, extensionRegistry, thisBuilder);
          }
          callback.onMerge(thisBuilder);
          // Iterate if we consume a separator.
        } while(tokenizer.tryConsume(JS_ARRAY_SEP_STR));

        // Make sure the next character terminates the array
        tokenizer.consume(JS_ARRAY_CLOSE_STR);
      }
    }
  }

}
