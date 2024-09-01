/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.upgrade.v5_0_x;

import org.obiba.core.util.FileUtil;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;

public class EhCacheFolderUpgradeStep extends AbstractUpgradeStep {
  private static final Logger log = LoggerFactory.getLogger(EhCacheFolderUpgradeStep.class);

  @Value("${OPAL_HOME}/work/ehcache")
  private String cachePath;

  @Override
  public void execute(Version version) {
    File folder = new File(cachePath);
    if (!folder.exists()) return;
    log.info("Clear EhCache folder: {}", cachePath);
    try {
      if (!FileUtil.delete(folder)) {
        log.warn("Cannot delete EhCache folder: {}", cachePath);
      }
    } catch (IOException e) {
      log.warn("Cannot delete EhCache folder: {}", cachePath, e);
    }
  }
}
