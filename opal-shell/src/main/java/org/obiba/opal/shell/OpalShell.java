package org.obiba.opal.shell;

/**
 * The {@code OpalShell} is used to interact with the user through input and output streams. Consider extending {@code
 * AbstractOpalShell} instead of implementing this interface directly.
 *
 * @see AbstractOpalShell
 */
public interface OpalShell extends Runnable {

  /**
   * Prints a message to the shell
   *
   * @param format message format as per {@code String#format(String, Object...)}
   * @param args message arguments
   */
  void printf(String format, Object... args);

  /**
   * Report progress of a command
   * @param message
   * @param current
   * @param end
   * @param percent
   */
  void progress(String message, long current, long end, int percent);

  /**
   * Prompts the shell for a password.
   *
   * @param format prompt message format
   * @param args prompt message arguments
   * @return the password as entered on the shell
   */
  char[] passwordPrompt(String format, Object... args);

  /**
   * Prompts the shell for an input.
   *
   * @param format prompt message format
   * @param args prompt message arguments
   * @return the text as entered on the shell
   */
  String prompt(String format, Object... args);

  /**
   * Prints the usage to the shell
   */
  void printUsage();

  /**
   * Tell the shell to exit.
   */
  void exit();

  /**
   * Adds a callback instance to be notified when the shell exists.
   *
   * @param callback
   */
  void addExitCallback(OpalShellExitCallback callback);

}
