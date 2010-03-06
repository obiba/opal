package org.obiba.opal.shell.jline;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import jline.ConsoleReader;

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
      consoleReader = new ConsoleReader(in, out);
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
      return consoleReader.readLine(String.format(format, args), new Character((char) 0)).toCharArray();
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
