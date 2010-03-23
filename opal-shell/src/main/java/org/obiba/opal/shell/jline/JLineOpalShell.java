package org.obiba.opal.shell.jline;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import jline.ConsoleReader;
import jline.Terminal;
import jline.UnixTerminal;

import org.obiba.opal.shell.AbstractOpalShell;
import org.obiba.opal.shell.CommandRegistry;

/**
 * Implements {@code OpalShell} using {@code JLine}
 */
public class JLineOpalShell extends AbstractOpalShell {

  private final ConsoleReader consoleReader;

  public JLineOpalShell(CommandRegistry registry, InputStream in, Writer out) {
    super(registry);
    try {

      // OPAL-222
      // Forcing a Unix terminal to fix a Putty compatibility issue when connecting from Windows through SSH.
      Terminal terminal = new UnixTerminal();
      consoleReader = new ConsoleReader(in, out, null, terminal);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void printf(String format, Object... args) {
    try {
      consoleReader.printString(String.format(format, args));
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  public char[] passwordPrompt(String format, Object... args) {
    try {
      return consoleReader.readLine(String.format(format, args), Character.valueOf((char) 0)).toCharArray();
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  public String prompt(String format, Object... args) {
    try {
      return consoleReader.readLine(String.format(format, args));
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }
}
