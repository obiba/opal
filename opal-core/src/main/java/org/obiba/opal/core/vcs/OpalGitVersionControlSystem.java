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

  @Autowired
  private GitCommandHandler gitCommandHandler;

  @Override
  public Iterable<CommitInfo> getCommitsInfo(@NotNull String datasource, @NotNull String path) {
    return gitCommandHandler.execute(new LogsCommand.Builder(OpalGitUtils.getGitViewsRepoFolder(datasource),
            OpalGitUtils.getGitViewsWorkFolder()).path(path).excludeDeletedCommits(true).build()
    );
  }

  @Override
  public CommitInfo getCommitInfo(@NotNull String datasource, @NotNull String path, @NotNull String commitId) {
    return gitCommandHandler.execute(
        new CommitLogCommand.Builder(OpalGitUtils.getGitViewsRepoFolder(datasource),
            OpalGitUtils.getGitViewsWorkFolder(), path, commitId).build());
  }

  @Override
  public String getBlob(@NotNull String datasource, @NotNull String path, @NotNull String commitId) {
    return gitCommandHandler.execute(
        new FetchBlobCommand.Builder(OpalGitUtils.getGitViewsRepoFolder(datasource),
            OpalGitUtils.getGitViewsWorkFolder(), path).commitId(commitId).build());
  }

  @Override
  public Iterable<String> getDiffEntries(@NotNull String datasource, @NotNull String commitId,
      @Nullable String prevCommitId, @Nullable String path) {
    return gitCommandHandler.execute(
        new DiffAsStringCommand.Builder(OpalGitUtils.getGitViewsRepoFolder(datasource),
            OpalGitUtils.getGitViewsWorkFolder(), commitId).path(path).previousCommitId(prevCommitId).build()
    );
  }

}

