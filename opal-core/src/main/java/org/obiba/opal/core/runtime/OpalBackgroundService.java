/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OpalBackgroundService implements Service {

  private boolean isRunning;

  private Collection<Thread> pool = new ArrayList<Thread>();

  private final Set<BackgroundTask> tasks;

  @Autowired
  public OpalBackgroundService(Set<BackgroundTask> tasks) {
    this.tasks = tasks;
  }

  @Override
  public boolean isRunning() {
    return isRunning;
  }

  @Override
  public void start() {
    if(!isRunning) {
      pool.clear();
      for(BackgroundTask task : tasks) {
        pool.add(startTask(task));
      }
      isRunning = true;
    }
  }

  @Override
  public void stop() {
    if(isRunning) {
      for(Thread background : pool) {
        try {
          background.interrupt();
        } finally {

        }
      }
      pool.clear();
    }
  }

  @Override
  public String getName() {
    return "background";
  }

  @Override
  public OpalConfigurationExtension getConfig() throws NoSuchServiceConfigurationException {
    throw new NoSuchServiceConfigurationException(getName());
  }

  private Thread startTask(BackgroundTask task) {
    Thread background = new Thread(task);
    background.setPriority(task.getPriority());
    background.start();
    return background;
  }

}
