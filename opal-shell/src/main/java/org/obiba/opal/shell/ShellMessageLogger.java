package org.obiba.opal.shell;

import org.obiba.opal.core.service.MessageLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by carlos on 7/29/14.
 */
public class ShellMessageLogger implements MessageLogger {

    private final OpalShell shell;
    private static final Logger log = LoggerFactory.getLogger(ShellMessageLogger.class);

    public ShellMessageLogger(OpalShell shell) {
        this.shell = shell;
    }

    @Override
    public void info(String msg, Object... args) {
        shell.printf(msg, args);
    }

    @Override
    public void warning(String msg, Object... args) {
        shell.printf("WARNING: " + msg, args);
    }

    @Override
    public void error(Throwable exception, String msg, Object... args) {
        log.error(msg, exception);
        shell.printf("ERROR: " + msg, args);
    }
}
