package org.obiba.opal.shell;

import org.obiba.opal.core.support.MessageLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Impl of MessageLogger that delegates all the messages to an OpalShell.
 * The DEBUG activation is tied to the actual slf4j Logger level of this class.
 * All the other types of messages are enabled unconditionally.
 */
public class OpalShellMessageAdapter implements MessageLogger {

    private static final Logger log = LoggerFactory.getLogger(OpalShellMessageAdapter.class);
    private final OpalShell shell;

    public OpalShellMessageAdapter(OpalShell shell) {
        this.shell = shell;
    }

    @Override
    public void info(String msg, Object... args) {
        out(msg, args);
    }

    @Override
    public void warn(String msg, Object... args) {
        out("WARNING: " + msg, args);
    }

    @Override
    public void error(String msg, Object... args) {
        out("ERROR: " + msg, args);
    }

    @Override
    public void debug(String msg, Object... args) {
        if (log.isDebugEnabled()) {
            shell.printf("DEBUG: "+ msg, args);
        }
    }

    private void out(String msg, Object ... args) {
        shell.printf(msg + "\n", args);
    }
}
