package org.obiba.opal.shell;

/**
 * The {@code OpalShell} is used to interact with the user through input and output streams.
 */
public interface OpalShell extends Runnable {

  /**
   * Prints a message to the shell
   * @param format message format as per {@code String#format(String, Object...)}
   * @param args message arguments
   */
  public void printf(String format, Object... args);

  /**
   * Prompts the shell for a password.
   * 
   * @param format prompt message format
   * @param args prompt message arguments
   * @return the password as entered on the shell
   */
  public char[] passwordPrompt(String format, Object... args);

  /**
   * Prompts the shell for an input.
   * 
   * @param format prompt message format
   * @param args prompt message arguments
   * @return the text as entered on the shell
   */
  public String prompt(String format, Object... args);

  /**
   * Prints the usage to the shell
   */
  public void printUsage();

  /**
   * Tell the shell to exit.
   */
  public void exit();

  /**
   * Adds a callback instance to be notified when the shell exists.
   * @param callback
   */
  public void addExitCallback(OpalShellExitCallback callback);

}
