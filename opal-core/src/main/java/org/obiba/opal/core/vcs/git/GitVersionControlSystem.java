package org.obiba.opal.core.vcs.git;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.obiba.opal.core.vcs.CommitInfo;
import org.obiba.opal.core.vcs.VersionControlSystem;
import org.obiba.opal.core.vcs.support.OpalGitUtils;
import org.springframework.stereotype.Component;

@Component
public class GitVersionControlSystem implements VersionControlSystem {

  private File repositoryRootPath;

  public List<CommitInfo> getViewCommitsInfo(@Nonnull String datasource, @Nonnull String view) {
    File repo  = OpalGitUtils.getGitDirectoryName(OpalGitUtils.buildOpalGitRootPath(), datasource);
    FileRepositoryBuilder builder = new FileRepositoryBuilder();
    List<CommitInfo> commits = new ArrayList<CommitInfo>();

    try {
      Repository repository = builder.setGitDir(repo)
          .readEnvironment() // scan environment GIT_* variables
          .findGitDir() // scan up the file system tree
          .build();

      Git git = new Git(repository);

      Iterable<RevCommit> log = git.log().call();

      for (RevCommit commit : log) {
        PersonIdent personIdent = commit.getAuthorIdent();
        commits.add(new CommitInfo.Builder().setAuthor(personIdent.getName()).setDate(personIdent.getWhen())
            .setComment(commit.getFullMessage()).build());
      }

    } catch(IOException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    } catch(NoHeadException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    } catch(GitAPIException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }

    return commits;
  }

}
