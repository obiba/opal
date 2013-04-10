/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.runtime.upgrade;

import java.io.File;
import java.io.IOException;

import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * Empty Elastic Search data directory (${OPAL_HOME}/data)
 */
public class ClearElasticSearchIndexes extends AbstractUpgradeStep {

  private static final Logger log = LoggerFactory.getLogger(ClearElasticSearchIndexes.class);

  @Value("${OPAL_HOME}/data")
  private String indexPath;

  @Override
  public void execute(Version currentVersion) {
    log.info("Clear Elastic Search indexes: {}", indexPath);
    try {
      File indexDir = new File(indexPath);
      if(indexDir.exists() && indexDir.isDirectory()) {
        deleteDirectoryContents(indexDir);
      } else {
        log.warn("Cannot find Elastic Search indexes: {}", indexPath);
      }
    } catch(IOException e) {
      throw new RuntimeException("Error while clearing Elastic Search indexes " + indexPath, e);
    }
  }

  /**
   * Copied form deprecated com.google.common.io.Files#deleteDirectoryContents(java.io.File) as we don't have symlinks here
   *
   * @param directory
   * @throws IOException
   */
  private static void deleteDirectoryContents(File directory) throws IOException {
    File[] files = directory.listFiles();
    if(files == null) {
      throw new IOException("Error listing files for " + directory);
    }
    for(File file : files) {
      deleteRecursively(file);
    }
  }

  /**
   * Copied form deprecated com.google.common.io.Files#deleteRecursively(java.io.File) as we don't have symlinks here
   *
   * @param directory
   * @throws IOException
   */
  private static void deleteRecursively(File file) throws IOException {
    if(file.isDirectory()) {
      deleteDirectoryContents(file);
    }
    if(!file.delete()) {
      throw new IOException("Failed to delete " + file);
    }
  }

  public void setIndexPath(String indexPath) {
    this.indexPath = indexPath;
  }
}
