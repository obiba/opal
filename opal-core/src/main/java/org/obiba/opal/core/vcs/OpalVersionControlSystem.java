/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.vcs;

import jakarta.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.git.CommitInfo;

public interface OpalVersionControlSystem {

  Iterable<CommitInfo> getCommitsInfo(@NotNull String datasource, @NotNull String path);

  CommitInfo getCommitInfo(@NotNull String datasource, @NotNull String path, @NotNull String commitId);

  String getBlob(@NotNull String datasource, @NotNull String path, @NotNull String commitId);

  Iterable<String> getDiffEntries(@NotNull String datasource, @NotNull String commitId, @Nullable String prevCommitId,
      @Nullable String path);
}
