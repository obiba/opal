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

import javax.validation.constraints.NotNull;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.obiba.opal.core.vcs.CommitInfo;
import org.obiba.opal.core.vcs.OpalGitException;

import com.google.common.base.Strings;

/**
 * Opal GIT command used to extract the log of a repository path for a specific commit.
 */
public class OpalGitCommitLogCommand extends OpalGitCommand<CommitInfo> {

  private String path;

  private String commitId;

  private OpalGitCommitLogCommand(Builder builder) {
    super(builder.repository, builder.datasourceName);
    commitId = builder.commitId;
    path = builder.path;
  }

  @Override
  public CommitInfo execute() {
    RevWalk walk = new RevWalk(repository);

    try {
      RevCommit commit = walk.parseCommit(ObjectId.fromString(commitId));
      if(TreeWalk.forPath(repository, path, commit.getTree()) != null) {
        // There is indeed the path in this commit
        PersonIdent personIdent = commit.getAuthorIdent();
        return new CommitInfo.Builder().setAuthor(personIdent.getName()).setDate(personIdent.getWhen())
            .setComment(commit.getFullMessage()).setCommitId(commit.getName()).setIsHead(isHead(commitId)).build();

      }
    } catch(IOException e) {
      throw new OpalGitException(e.getMessage(), e);
    }

    throw new OpalGitException(String.format("Path '%s' was not found in commit '%s'", path, commitId));
  }

  /**
   * Builder class for OpalGitCommitLogCommand
   */
  public static class Builder extends OpalGitCommand.Builder<Builder> {

    private final String commitId;

    public Builder(@NotNull Repository repository, @NotNull String path, @NotNull String commitId) {
      super(repository);
      addPath(path);
      this.commitId = commitId;
    }

    public OpalGitCommitLogCommand build() {
      if(Strings.isNullOrEmpty(path)) throw new OpalGitException("Commit path cannot empty nor null.");
      if(Strings.isNullOrEmpty(commitId)) throw new OpalGitException("Commit id can not be empty nor null.");
      return new OpalGitCommitLogCommand(this);
    }
  }

}