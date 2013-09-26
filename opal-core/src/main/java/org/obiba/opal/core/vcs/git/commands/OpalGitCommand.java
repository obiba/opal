package org.obiba.opal.core.vcs.git.commands;

import java.io.IOException;

import javax.annotation.Nonnull;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.obiba.opal.core.vcs.OpalGitException;

import edu.umd.cs.findbugs.annotations.Nullable;

public abstract class OpalGitCommand<T> implements Command<T> {

  protected final Repository repository;
  protected final String datasourceName;

  protected OpalGitCommand(Repository repository) {
    this(repository, "");
  }

  protected OpalGitCommand(@Nonnull Repository repository, @Nullable String datasourceName) {
    if (repository == null) {
      throw new OpalGitException("Repository cannot be NULL!");
    }

    this.repository = repository;
    this.datasourceName = datasourceName;
  }

  protected void validate() {}

  protected RevCommit getCommitById(String commitId) throws IOException {
    ObjectId id = repository.resolve(commitId);

    if (id != null) {
      return new RevWalk(repository).parseCommit(id);
    }

    return null;
  }

}
