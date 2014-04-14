/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.vcs;

import java.io.File;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.git.CommitInfo;
import org.obiba.git.command.CommitLogCommand;
import org.obiba.git.command.DiffAsStringCommand;
import org.obiba.git.command.FetchBlobCommand;
import org.obiba.git.command.GitCommandHandler;
import org.obiba.git.command.LogsCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OpalGitVersionControlSystem implements OpalVersionControlSystem {

  private static final String GIT_ROOT_PATH = "data" + File.separator + "git" + File.separator +
      "views";

  private final File repoPath;

  @Autowired
  private GitCommandHandler gitCommandHandler;

  public OpalGitVersionControlSystem() {
    this(new File(System.getProperty("OPAL_HOME"), GIT_ROOT_PATH));
  }

  public OpalGitVersionControlSystem(File repoPath) {
    this.repoPath = repoPath;
  }

  @Override
  public Iterable<CommitInfo> getCommitsInfo(@NotNull String datasource, @NotNull String path) {
//    OpalGitCommitsLogCommand command = new OpalGitCommitsLogCommand.Builder(getRepository(datasource)).addPath(path)
//        .addDatasourceName(datasource).build();
    //TODO support datasourceName
    return gitCommandHandler.execute(new LogsCommand.Builder(repoPath).path(path).build());
  }

  @Override
  public CommitInfo getCommitInfo(@NotNull String datasource, @NotNull String path, @NotNull String commitId) {
//    OpalGitCommitLogCommand command = new OpalGitCommitLogCommand.Builder(getRepository(datasource), path, commitId)
//        .addDatasourceName(datasource).build();
    //TODO support datasourceName
    return gitCommandHandler.execute(new CommitLogCommand.Builder(repoPath, path, commitId).build());
  }

  @Override
  public String getBlob(@NotNull String datasource, @NotNull String path, @NotNull String commitId) {
//    OpalGitFetchBlobCommand command = new OpalGitFetchBlobCommand.Builder(getRepository(datasource), path, commitId)
//        .addDatasourceName(datasource).build();
    //TODO support datasourceName
    return gitCommandHandler.execute(new FetchBlobCommand.Builder(repoPath, path).commitId(commitId).build());
  }

  @Override
  public Iterable<String> getDiffEntries(@NotNull String datasource, @NotNull String commitId,
      @Nullable String prevCommitId, @Nullable String path) {
//    OpalGitDiffCommand command = new OpalGitDiffCommand.Builder(getRepository(datasource), commitId).addPath(path)
//        .addDatasourceName(datasource).addPreviousCommitId(prevCommitId).build();
    //TODO support datasourceName
    return gitCommandHandler
        .execute(new DiffAsStringCommand.Builder(repoPath, commitId).path(path).previousCommitId(prevCommitId).build());
  }

}

