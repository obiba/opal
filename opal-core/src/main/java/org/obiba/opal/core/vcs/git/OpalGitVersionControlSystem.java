package org.obiba.opal.core.vcs.git;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.obiba.opal.core.vcs.CommitInfo;
import org.obiba.opal.core.vcs.OpalGitException;
import org.obiba.opal.core.vcs.OpalVersionControlSystem;
import org.obiba.opal.core.vcs.git.commands.OpalGitCommitLogCommand;
import org.obiba.opal.core.vcs.git.commands.OpalGitCommitsLogCommand;
import org.obiba.opal.core.vcs.git.commands.OpalGitDiffCommand;
import org.obiba.opal.core.vcs.git.commands.OpalGitFetchBlobCommand;
import org.obiba.opal.core.vcs.support.OpalGitUtils;
import org.springframework.stereotype.Component;

@Component
public class OpalGitVersionControlSystem implements OpalVersionControlSystem {

  @Override
  public List<CommitInfo> getCommitsInfo(@Nonnull String datasource, @Nonnull String path) {
    OpalGitCommitsLogCommand command = new OpalGitCommitsLogCommand.Builder(getRepository(datasource)).addPath(path)
        .addDatasourceName(datasource).build();
    return command.execute();
  }

  @Override
  public CommitInfo getCommitInfo(@Nonnull String datasource, @Nonnull String path, @Nonnull String commitId) {
    OpalGitCommitLogCommand command = new OpalGitCommitLogCommand.Builder(getRepository(datasource), path, commitId)
        .addDatasourceName(datasource).build();
    return command.execute();
  }

  @Override
  public String getBlob(@Nonnull String datasource, @Nonnull String path, @Nonnull String commitId) {
    OpalGitFetchBlobCommand command = new OpalGitFetchBlobCommand.Builder(getRepository(datasource), path, commitId)
        .addDatasourceName(datasource).build();
    return command.execute();
  }

  @Override
  public List<String> getDiffEntries(@Nonnull String datasource, @Nonnull String commitId, String path) {
    OpalGitDiffCommand command = new OpalGitDiffCommand.Builder(getRepository(datasource), commitId).addPath(path)
        .addDatasourceName(datasource).build();

    return command.execute();
  }

  protected Repository getRepository(String name) {
    File repo = OpalGitUtils.getGitDirectoryName(OpalGitUtils.buildOpalGitRootPath(), name);
    FileRepositoryBuilder builder = new FileRepositoryBuilder();

    try {
      return builder.setGitDir(repo).readEnvironment().findGitDir().build();
    } catch(IOException e) {
      throw new OpalGitException(e.getMessage(), e);
    }
  }

}

