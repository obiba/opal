package org.obiba.opal.shell.commands;

/**
 * Enhanced Command, that produces a result and might want to be informed of the job id for reference.
 */
public interface ExtendedCommand<T,R> extends Command<T> {

    /**
     * Sets the job id where this command is being run
     * @param id
     */
    void setJobId(int id);

    /**
     * Shoudl be called tipically after the command was run, never before.
     * @return result of the command, or null if not ready yet.
     */
    R getResult();

}
