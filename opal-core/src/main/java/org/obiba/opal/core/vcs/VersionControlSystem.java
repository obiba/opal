package org.obiba.opal.core.vcs;

import java.util.List;

public interface VersionControlSystem {
  List<CommitInfo> getViewCommitsInfo(String datasource, String view);
}
