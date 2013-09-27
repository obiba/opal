package org.obiba.opal.core.vcs;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface OpalVersionControlSystem {
  List<CommitInfo> getCommitsInfo(@Nonnull String datasource, @Nonnull String path);
  CommitInfo getCommitInfo(@Nonnull String datasource, @Nonnull String path, @Nonnull String commitId);
  String getBlob(@Nonnull String datasource, @Nonnull String path, @Nonnull String commitId);
  List<String> getDiffEntries(@Nonnull String datasource, @Nonnull String commitId, @Nullable String path);
}
