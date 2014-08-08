package org.obiba.opal.core.support;

/**
 * Created by carlos on 8/8/14.
 */
public interface MessageListener {

    void debug(String msgFormat, Object ... args);

    void info(String msgFormat, Object ... args);

    void warn(String msgFormat, Object ... args);

    void error(String msgFormat, Object ... args);

}
