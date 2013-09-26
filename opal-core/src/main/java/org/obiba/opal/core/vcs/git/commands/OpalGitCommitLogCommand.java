package org.obiba.opal.core.vcs.git.commands;

import java.io.IOException;

import javax.annotation.Nonnull;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.obiba.opal.core.vcs.CommitInfo;
import org.obiba.opal.core.vcs.OpalGitException;

public class OpalGitCommitLogCommand extends OpalGitCommand<CommitInfo> {

  private String path;
  private String commitId;


  public OpalGitCommitLogCommand(@Nonnull Repository repository, String datasourceName) {
    super(repository, datasourceName);
  }
 public OpalGitCommitLogCommand(@Nonnull Repository repository) {
    super(repository);
  }

  public OpalGitCommitLogCommand addPath(String value) {
    path = value;
    return this;
  }

  public OpalGitCommitLogCommand addCommitId(String value) {
    commitId = value;
    return this;
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
            .setComment(commit.getFullMessage()).setCommitId(commit.getName()).build();

      }
    } catch(IOException e) {
      throw new OpalGitException(e.getMessage(), e);
    }

    throw new OpalGitException(String.format("Path '%s' was not found in commit '%s'", path, commitId));
  }

}