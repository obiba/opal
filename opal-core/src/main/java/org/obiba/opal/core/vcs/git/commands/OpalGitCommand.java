package org.obiba.opal.core.vcs.git.commands;

import java.io.IOException;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public abstract class OpalGitCommand<T> implements Command<T> {

  protected final Repository repository;

  protected OpalGitCommand(Repository repository) {
    this.repository = repository;
  }

  protected RevCommit getCommitById(String commitId) throws IOException {
    ObjectId id = repository.resolve(commitId);

    if (id != null) {
      return new RevWalk(repository).parseCommit(id);
    }

    return null;
  }

}
