package org.obiba.opal.core.vcs.git.commands;

import org.eclipse.jgit.lib.Repository;

public abstract class OpalGitCommand<T> implements Command<T> {

  protected final Repository repository;

  protected OpalGitCommand(Repository repository) {
    this.repository = repository;
  }

}
