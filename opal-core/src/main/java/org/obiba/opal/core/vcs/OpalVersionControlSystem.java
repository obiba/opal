package org.obiba.opal.core.vcs;

import java.util.List;

import javax.annotation.Nonnull;

public interface OpalVersionControlSystem {
  List<CommitInfo> getCommitsInfo(@Nonnull String datasource, @Nonnull String path);
  CommitInfo getCommitInfo(@Nonnull String datasource, @Nonnull String path, @Nonnull String commitId);
  String getBlob(@Nonnull String datasource, @Nonnull String path, @Nonnull String commitId);
}
