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

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface OpalVersionControlSystem {
  List<CommitInfo> getCommitsInfo(@Nonnull String datasource, @Nonnull String path);

  CommitInfo getCommitInfo(@Nonnull String datasource, @Nonnull String path, @Nonnull String commitId);

  String getBlob(@Nonnull String datasource, @Nonnull String path, @Nonnull String commitId);

  List<String> getDiffEntries(@Nonnull String datasource, @Nonnull String commitId, @Nullable String prevCmmitId,
      @Nullable String path);
}
