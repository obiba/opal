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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

import edu.umd.cs.findbugs.annotations.Nullable;

@Component
public class OpalGitVersionControlSystem implements OpalVersionControlSystem {

  private static final Logger log = LoggerFactory.getLogger(OpalGitVersionControlSystem.class);

  public List<CommitInfo> getCommitsInfo(@Nonnull String datasource, @Nonnull String path) {
    OpalGitCommitsLogCommand command = new OpalGitCommitsLogCommand(getRepository(datasource), datasource).addPath(path);
    return command.execute();
  }

  public CommitInfo getCommitInfo(@Nonnull String datasource, @Nonnull String path, @Nonnull String commitId) {
    OpalGitCommitLogCommand command = new OpalGitCommitLogCommand(getRepository(datasource), datasource).addPath(path)
        .addCommitId(commitId);
    return command.execute();
  }

  public String getBlob(@Nonnull String datasource, @Nonnull String path, @Nonnull String commitId) {
    OpalGitFetchBlobCommand command = new OpalGitFetchBlobCommand(getRepository(datasource), datasource).addPath(path)
        .addCommitId(commitId);
    return command.execute();
  }

  public List<String> getDiffEntries(@Nonnull String datasource, @Nonnull String commitId, @Nullable String path) {
    OpalGitDiffCommand command = new OpalGitDiffCommand(getRepository(datasource), datasource).addCommitId(commitId);
    if (Strings.isNullOrEmpty(path)) command.addPath(path);

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

