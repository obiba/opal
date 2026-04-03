package org.obiba.opal.shell.web;

import com.google.common.base.Strings;
import org.obiba.opal.shell.commands.options.FileBundleCommandOptions;

public class FileBundleCommandOptionsImpl implements FileBundleCommandOptions {

  private final String path;

  private final String password;

  public FileBundleCommandOptionsImpl(String path, String password) {
    this.path = path;
    this.password = password;
  }

  @Override
  public String getPath() {
    return path;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public boolean isPassword() {
    return !Strings.isNullOrEmpty(password);
  }

  @Override
  public boolean isHelp() {
    return false;
  }
}
