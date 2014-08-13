package org.obiba.opal.core.support;

import org.slf4j.Logger;

/**
 * Impl of MessageLogger that delegates all the messages to a given slf4j logger.
 * All messages are directly delegated, so it depends on the level configuration.
 */
public class Slf4jMessageAdapter implements MessageLogger {

    private final Logger delegate;

    public Slf4jMessageAdapter(Logger delegate) {
        this.delegate = delegate;
    }

    @Override
    public void debug(String msgFormat, Object... args) {
        delegate.debug(String.format(msgFormat, args));
    }

    @Override
    public void info(String msgFormat, Object... args) {
        delegate.info(String.format(msgFormat, args));
    }

    @Override
    public void warn(String msgFormat, Object... args) {
        delegate.warn(String.format(msgFormat, args));
    }

    @Override
    public void error(String msgFormat, Object... args) {
        delegate.error(String.format(msgFormat, args));
    }

}
