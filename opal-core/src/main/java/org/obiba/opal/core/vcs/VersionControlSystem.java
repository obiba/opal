package org.obiba.opal.core.vcs;

import java.util.List;

import javax.annotation.Nonnull;

public interface VersionControlSystem {
  List<CommitInfo> getViewCommitsInfo(@Nonnull String datasource, @Nonnull String view);
  List<CommitInfo> getViewCommitInfo(@Nonnull String datasource, @Nonnull String view, @Nonnull String commitId);
}
