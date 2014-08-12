package org.obiba.opal.core.support;

/**
 * Generic logger for messages.
 */
public interface MessageLogger {

    public static String MESSAGE_FORMAT = "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %2$s %3$s";

    void debug(String msgFormat, Object ... args);

    void info(String msgFormat, Object ... args);

    void warn(String msgFormat, Object ... args);

    void error(String msgFormat, Object ... args);

}
