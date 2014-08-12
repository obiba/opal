package org.obiba.opal.core.support;

import java.util.Date;

/**
 Impl of MessageListener that just System.out.println all the messages
 */
public class SystemOutMessageLogger implements MessageLogger {

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
