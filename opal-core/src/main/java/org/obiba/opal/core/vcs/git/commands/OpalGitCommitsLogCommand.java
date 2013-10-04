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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.obiba.opal.core.vcs.CommitInfo;
import org.obiba.opal.core.vcs.OpalGitException;

import com.google.common.base.Strings;

/**
 * Opal GIT command used to extract a list of logs of a repository path.
 */
public class OpalGitCommitsLogCommand extends OpalGitCommand<List<CommitInfo>> {

  private String path;

  private OpalGitCommitsLogCommand(Builder builder) {
    super(builder.repository, builder.datasourceName);
    path = builder.path;
  }

  @Override
  public List<CommitInfo> execute() {
    try {
      Git git = new Git(repository);
      LogCommand logCommand = git.log();

      if(!Strings.isNullOrEmpty(path)) {
        logCommand.addPath(path);
      }

      Iterable<RevCommit> commitLog = logCommand.call();
      List<CommitInfo> commits = new ArrayList<CommitInfo>();

      for(RevCommit commit : commitLog) {
        PersonIdent personIdent = commit.getAuthorIdent();
        commits.add(new CommitInfo.Builder().setAuthor(personIdent.getName()).setDate(personIdent.getWhen())
            .setComment(commit.getFullMessage()).setCommitId(commit.getName()).build());
      }

      if(commits.isEmpty()) {
        throw new OpalGitException(getNoCommitsErrorMessage());
      }

      return commits;
    } catch(GitAPIException e) {
      throw new OpalGitException(e.getMessage(), e);
    }
  }

  private String getNoCommitsErrorMessage() {
    String errorMessage = String.format("There are no commits in '%s' repository",
        Strings.isNullOrEmpty(datasourceName) ? datasourceName : "this");

    if(Strings.isNullOrEmpty(path)) {
      errorMessage += String.format(" for path '%s'", path);
    }

    return errorMessage;
  }

  /**
   * Builder class for OpalGitCommitsLogCommand
   */
  public static class Builder extends OpalGitCommand.Builder<Builder> {

    public Builder(@Nonnull Repository repository) {
      super(repository);
    }

    public OpalGitCommitsLogCommand build() {
      return new OpalGitCommitsLogCommand(this);
    }
  }

}
