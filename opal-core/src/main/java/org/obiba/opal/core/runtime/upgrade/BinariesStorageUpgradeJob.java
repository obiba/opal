/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.runtime.upgrade;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.opal.core.runtime.BackgroundJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BinariesStorageUpgradeJob implements BackgroundJob {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private int progress;

  private String progressStatus;

  @Override
  public void run() {
    try {
      while(true) {
        Thread.sleep(5000);
        for(Datasource ds : MagmaEngine.get().getDatasources()) {
          if (ds.getType().equals("hibernate")) {
            process(ds);
          }
        }
      }
    } catch(Exception e) {

    }
  }

  @Override
  public String getName() {
    return "BinariesStorageUpgrade";
  }

  @Override
  public String getDescription() {
    return "Moves the binary values from an base64 encoded string to a blob in Hibernate Datasources.";
  }

  @Override
  public int getPriority() {
    return Thread.MIN_PRIORITY;
  }

  @Override
  public int getProgress() {
    return 0;
  }

  @Override
  public String getProgressStatus() {
    return progressStatus;
  }

  private void process(Datasource datasource) {
    log.info("Scanning for value sets with binary values to be upgraded in datasource {}", datasource.getName());
    for (ValueTable table : datasource.getValueTables()) {
      log.info("  {}", table.getName());
    }
  }
}
