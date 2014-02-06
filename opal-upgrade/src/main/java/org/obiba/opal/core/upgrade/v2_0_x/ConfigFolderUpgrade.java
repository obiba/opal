package org.obiba.opal.core.upgrade.v2_0_x;

import java.io.File;
import java.io.IOException;

import org.obiba.core.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigFolderUpgrade {

  private static final Logger log = LoggerFactory.getLogger(ConfigFolderUpgrade.class);

  private ConfigFolderUpgrade() {}

  @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
  public static void cleanDirectories() {

    String opalHome = System.getenv().get("OPAL_HOME");

    try {
      FileUtil.delete(new File(opalHome, "data" + File.separatorChar + "opal"));
    } catch(IOException e) {
      log.warn("Cannot delete 'data/opal' dir", e);
    }

    try {
      FileUtil.delete(new File(opalHome, "work"));
    } catch(IOException e) {
      log.warn("Cannot delete 'work' dir", e);
    }

  }

}
