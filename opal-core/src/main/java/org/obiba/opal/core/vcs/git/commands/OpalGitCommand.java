/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.vcs.git.commands;

import java.io.IOException;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.obiba.opal.core.vcs.git.support.GitUtils;

import com.google.common.base.Preconditions;

/**
 * Base class for all Opal GIT commands. All subclasses are immutables and must be created by their respective builders
 *
 * @param <T> type of builder
 */
public abstract class OpalGitCommand<T> implements Command<T> {

  final Repository repository;

  final String datasourceName;

  OpalGitCommand(Repository repository) {
    this(repository, "");
  }

  OpalGitCommand(@NotNull Repository repository, @Nullable String datasourceName) {
    this.repository = repository;
    this.datasourceName = datasourceName;
  }

  @Nullable
  RevCommit getCommitById(String commitId) throws IOException {
    ObjectId id = repository.resolve(commitId);

    if(id != null) {
      return new RevWalk(repository).parseCommit(id);
    }

    return null;
  }

  boolean isHead(String commitId) throws IOException {
    return GitUtils.HEAD_COMMIT_ID.equals(commitId) || getHeadCommitId().equals(commitId);
  }

  ObjectId getHeadCommit() throws IOException {
    return repository.resolve(GitUtils.HEAD_COMMIT_ID);
  }

  String getHeadCommitId() throws IOException {
    ObjectId id = getHeadCommit();
    return id == null ? "" : id.getName();
  }

  /**
   * Base class for all command builder.
   *
   * @param <T> subclass type
   */
  protected static class Builder<T extends Builder<?>> {

    final Repository repository;

    String path;

    String datasourceName; // used mainly for debug and meaningful error messages

    @SuppressWarnings("ConstantConditions")
    Builder(@NotNull Repository repository) {
      Preconditions.checkArgument(repository != null, "Repository cannot be null.");
      this.repository = repository;
    }

    @SuppressWarnings("unchecked")
    public T addPath(@Nullable String path) {
      this.path = path;
      return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T addDatasourceName(@NotNull String datasourceName) {
      this.datasourceName = datasourceName;
      return (T) this;
    }
  }

}
