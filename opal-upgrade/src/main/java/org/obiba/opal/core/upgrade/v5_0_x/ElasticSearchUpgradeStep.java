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

public class ElasticSearchUpgradeStep extends AbstractUpgradeStep {
  private static final Logger log = LoggerFactory.getLogger(ElasticSearchUpgradeStep.class);

  @Value("${OPAL_HOME}/work/opal-search-es")
  private String workPath;

  @Value("${OPAL_HOME}/data/opal-search-es")
  private String dataPath;

  @Override
  public void execute(Version version) {
    cleanFolder(workPath);
    cleanFolder(dataPath);
  }

  private void cleanFolder(String path) {
    File folder = new File(path);
    if (!folder.exists()) return;
    log.info("Clear Search ES folder: {}", path);
    try {
      if (!FileUtil.delete(folder)) {
        log.warn("Cannot delete Search ES folder: {}", path);
      }
    } catch (IOException e) {
      log.warn("Cannot delete Search ES folder: {}", path, e);
    }
  }
}
