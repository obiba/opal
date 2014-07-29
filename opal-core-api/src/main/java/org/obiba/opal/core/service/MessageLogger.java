package org.obiba.opal.core.service;

/**
 * Created by carlos on 7/29/14.
 */
public interface MessageLogger {

    void info(String msg, Object ... args);

    void warning(String msg, Object ... args);

    void error(Throwable exception, String msg, Object ... args);

}
