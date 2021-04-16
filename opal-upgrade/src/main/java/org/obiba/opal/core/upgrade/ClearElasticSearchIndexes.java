/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.upgrade;

import java.io.File;
import java.io.IOException;

import org.obiba.core.util.FileUtil;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * Empty Elastic Search data directory (${OPAL_HOME}/work/elastic-search)
 */
public class ClearElasticSearchIndexes extends AbstractUpgradeStep {

  private static final Logger log = LoggerFactory.getLogger(ClearElasticSearchIndexes.class);

  @Value("${OPAL_HOME}/work/elasticsearch")
  private String indexPath;

  @Override
  public void execute(Version currentVersion) {
    removeEsWorkDir(indexPath);
  }

  private void removeEsWorkDir(String indexPath) {
    File esWork = new File(indexPath);
    if (!esWork.exists()) return;
    log.info("Clear Elastic Search indexes: {}", indexPath);
    try {
      if(!FileUtil.delete(esWork)) {
        log.warn("Cannot find Elastic Search indexes: {}", indexPath);
      }
    } catch(IOException e) {
      throw new RuntimeException("Error while clearing Elastic Search indexes " + indexPath, e);
    }
  }

  public void setIndexPath(String indexPath) {
    this.indexPath = indexPath;
  }
}
