package org.obiba.opal.core.vcs.git.commands;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.obiba.opal.core.vcs.CommitInfo;
import org.obiba.opal.core.vcs.OpalGitException;

public class OpalGitCommitsLogCommand extends OpalGitCommand<List<CommitInfo>> {

  private String path;

  public OpalGitCommitsLogCommand(Repository repository) {
    super(repository);
  }

  public OpalGitCommitsLogCommand addPath(String value) {
    path = value;
    return this;
  }

  @Override
  public List<CommitInfo> execute() {
    List<CommitInfo> commits = new ArrayList<CommitInfo>();

    Git git = new Git(repository);
    LogCommand logCommand = git.log();
    logCommand.addPath(path);
    Iterable<RevCommit> commitLog = null;
    try {
      commitLog = logCommand.call();

      for(RevCommit commit : commitLog) {
        PersonIdent personIdent = commit.getAuthorIdent();
        commits.add(new CommitInfo.Builder().setAuthor(personIdent.getName()).setDate(personIdent.getWhen())
            .setComment(commit.getFullMessage()).setCommitId(commit.getName()).build());
      }
    } catch(GitAPIException e) {
      throw new OpalGitException(e.getMessage(), e);
    }

    return commits;
  }
}
