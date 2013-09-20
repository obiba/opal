package org.obiba.opal.core.vcs.git;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.obiba.opal.core.vcs.CommitInfo;
import org.obiba.opal.core.vcs.VersionControlSystem;
import org.obiba.opal.core.vcs.support.OpalGitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GitVersionControlSystem implements VersionControlSystem {

  private static final Logger log = LoggerFactory.getLogger(GitVersionControlSystem.class);

  public List<CommitInfo> getViewCommitsInfo(@Nonnull String datasource, @Nonnull String path) {
    File repo = OpalGitUtils.getGitDirectoryName(OpalGitUtils.buildOpalGitRootPath(), datasource);
    FileRepositoryBuilder builder = new FileRepositoryBuilder();
    List<CommitInfo> commits = new ArrayList<CommitInfo>();

    try {
      Repository repository = builder.setGitDir(repo).readEnvironment() // scan environment GIT_* variables
          .findGitDir() // scan up the file system tree
          .build();

      Git git = new Git(repository);
      LogCommand logCommand = git.log();
      logCommand.addPath(path);
      Iterable<RevCommit> commitLog = logCommand.call();

      for(RevCommit commit : commitLog) {
        log.info("Commit name : {}, Tree name: {}", commit.getName());
        PersonIdent personIdent = commit.getAuthorIdent();
        commits.add(new CommitInfo.Builder().setAuthor(personIdent.getName()).setDate(personIdent.getWhen())
            .setComment(commit.getFullMessage()).setCommitId(commit.getName()).build());
      }

    } catch(IncorrectObjectTypeException e1) {
      e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    } catch(IOException e1) {
      e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    } catch(NoHeadException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    } catch(GitAPIException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }

    return commits;
  }

  public List<CommitInfo> getViewCommitInfo(@Nonnull String datasource, @Nonnull String path, @Nonnull String commitId) {
    File repo = OpalGitUtils.getGitDirectoryName(OpalGitUtils.buildOpalGitRootPath(), datasource);
    FileRepositoryBuilder builder = new FileRepositoryBuilder();
    List<CommitInfo> commits = new ArrayList<CommitInfo>();

    try {
      Repository repository = builder.setGitDir(repo).readEnvironment() // scan environment GIT_* variables
          .findGitDir() // scan up the file system tree
          .build();

      Git git = new Git(repository);
      LogCommand logCommand = git.log();
      logCommand.addPath(path);
      ObjectId id = ObjectId.fromString(commitId);
//      logCommand.addRange(null, id);
      Iterable<RevCommit> commitLog = logCommand.call();

      for(RevCommit commit : commitLog) {
        log.info("Commit name : {}, Tree name: {}", commit.getName());
//        PersonIdent personIdent = commit.getAuthorIdent();
//        commits.add(new CommitInfo.Builder().setAuthor(personIdent.getName()).setDate(personIdent.getWhen())
//            .setComment(commit.getFullMessage()).setCommitId(commit.getName()).build());
      }

    } catch(IncorrectObjectTypeException e1) {
      e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    } catch(IOException e1) {
      e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    } catch(NoHeadException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    } catch(GitAPIException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }

    return commits;
  }

}

