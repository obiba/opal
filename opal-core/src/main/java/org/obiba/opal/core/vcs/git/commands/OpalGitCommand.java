package org.obiba.opal.core.vcs.git.commands;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.obiba.opal.core.vcs.OpalGitException;

/**
 * Base class for all Opal GIT commands. All subclasses are immutables and must be created by their respective builders
 *
 * @param <T> type of builder
 */
public abstract class OpalGitCommand<T> implements Command<T> {

  protected final Repository repository;
  protected final String datasourceName;

  protected OpalGitCommand(Repository repository) {
    this(repository, "");
  }

  protected OpalGitCommand(@Nonnull Repository repository, @Nullable String datasourceName) {
    this.repository = repository;
    this.datasourceName = datasourceName;
  }

  protected RevCommit getCommitById(String commitId) throws IOException {
    ObjectId id = repository.resolve(commitId);

    if (id != null) {
      return new RevWalk(repository).parseCommit(id);
    }

    return null;
  }

  /**
   * Base class for all command builder.
   * @param <T> subclass type
   */
  @SuppressWarnings("RawUseOfParameterizedType")
  protected static class Builder<T extends Builder> {
    protected final Repository repository;
    protected String path;
    protected String datasourceName; // used mainly for debug and meaningful error messages

    protected Builder(@Nonnull Repository repository) {
      if (repository == null) throw new OpalGitException("Repository cannot be null.");
      this.repository = repository;
    }

    public T addPath(@Nonnull String value) {
      path = value;
      return (T)this;
    }

    public T addDatasourceName(@Nonnull String value) {
      datasourceName = value;
      return (T)this;
    }
  }

}
