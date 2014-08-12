package org.obiba.opal.core.support;

import java.util.Date;

/**
 * Listener for generic messages.
 */
public interface MessageListener {

    public static String MESSAGE_FORMAT = "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %2$s %3$s";

    void debug(String msgFormat, Object ... args);

    void info(String msgFormat, Object ... args);

    void warn(String msgFormat, Object ... args);

    void error(String msgFormat, Object ... args);

    /**
     * Impl of MessageListener that just System.out.println all the messages
     */
    class NullMessageListener implements MessageListener {

        @Override
        public void debug(String msgFormat, Object... args) {
            add("DEBUG", msgFormat, args);
        }

        @Override
        public void info(String msgFormat, Object... args) {
            add("INFO", msgFormat, args);
        }

        @Override
        public void warn(String msgFormat, Object... args) {
            add("WARN", msgFormat, args);
        }

        @Override
        public void error(String msgFormat, Object... args) {
            add("ERROR", msgFormat, args);
        }

        private void add(String type, String msgFormat, Object... args) {
            String msg = msgFormat.format(msgFormat, args);
            String fullMsg = String.format(MESSAGE_FORMAT, new Date(), type, msg);
            System.out.println(fullMsg);
        }
    }

}
