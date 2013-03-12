/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.cfg;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DefaultOpalConfigurationService implements OpalConfigurationService {

  private final OpalConfigurationIo opalConfigIo;

  private final Lock opalConfigurationLock = new ReentrantLock();

  private final Condition opalConfigAvailable = opalConfigurationLock.newCondition();

  private OpalConfiguration opalConfiguration;

  @Autowired
  public DefaultOpalConfigurationService(OpalConfigurationIo opalConfigIo) {
    this.opalConfigIo = opalConfigIo;
  }

  public void readOpalConfiguration() {
    opalConfigurationLock.lock();
    try {
      opalConfiguration = opalConfigIo.readConfiguration();
      opalConfigAvailable.signalAll();
    } finally {
      opalConfigurationLock.unlock();
    }
  }

  @Override
  public OpalConfiguration getOpalConfiguration() {
    opalConfigurationLock.lock();
    try {
      while(opalConfiguration == null) {
        opalConfigAvailable.await();
      }
      return opalConfiguration;
    } catch(InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      opalConfigurationLock.unlock();
    }
  }

  @Override
  public void modifyConfiguration(ConfigModificationTask task) {
    opalConfigurationLock.lock();
    try {
      task.doWithConfig(getOpalConfiguration());
      writeOpalConfiguration();
    } finally {
      opalConfigurationLock.unlock();
    }
  }

  private void writeOpalConfiguration() {
    opalConfigurationLock.lock();
    try {
      opalConfigIo.writeConfiguration(opalConfiguration);
    } finally {
      opalConfigurationLock.unlock();
    }
  }

}
