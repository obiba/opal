package org.obiba.opal.search;

import java.util.Date;

/**
 *
 */
public class Message {

    private final long timestamp;
    private final String message;

    public Message(long timestamp, String message) {
        this.timestamp = timestamp;
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        Date time = new Date(timestamp);
        return String.format("%s: %s", time, message);
    }
}
