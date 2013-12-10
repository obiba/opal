package org.obiba.opal.core.vcs.git.support;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.obiba.opal.core.vcs.OpalGitException;
import org.obiba.opal.core.vcs.git.OpalGitVersionControlSystem;
import org.obiba.opal.core.vcs.support.OpalGitUtils;

public class TestOpalGitVersionControlSystem extends OpalGitVersionControlSystem {

  @Override
  public Repository getRepository(String name) {
    try {
      File repo = OpalGitUtils.getGitDirectoryName(getGitFolder(), name);
      FileRepositoryBuilder builder = new FileRepositoryBuilder();
      return builder.setGitDir(repo).readEnvironment().findGitDir().build();
    } catch(IOException | URISyntaxException e) {
      throw new OpalGitException(e.getMessage(), e);
    }
  }

  private File getGitFolder() throws URISyntaxException {
    File f = new File(getClass().getClassLoader().getResource("OpalVersionControlSystemTest/git/views").toURI());
    return f;
  }

}
