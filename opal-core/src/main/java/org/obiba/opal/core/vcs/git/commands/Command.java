package org.obiba.opal.core.vcs.git.commands;

public interface Command<T> {
  T execute();
}

