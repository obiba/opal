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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.core.runtime.security.BackgroundJobServiceAuthToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BackgroundJobService implements Service {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private boolean isRunning;

  private final Map<String,Thread> jobThreads = new HashMap<String, Thread>();

  private final Set<BackgroundJob> jobs;

  @Autowired
  public BackgroundJobService(Set<BackgroundJob> jobs) {
    this.jobs = jobs;
  }

  @Override
  public boolean isRunning() {
    return isRunning;
  }

  @Override
  public void start() {
    if(!isRunning) {
      jobThreads.clear();
      for(BackgroundJob job : jobs) {
        jobThreads.put(job.getName(), startJob(job));
      }
      isRunning = true;
    }
  }

  @Override
  public void stop() {
    if(isRunning) {
      for(Map.Entry<String,Thread> background : jobThreads.entrySet()) {
        try {
          if (background.getValue().isAlive()) {
            log.info("Interrupting job [{}]", background.getKey());
            background.getValue().interrupt();
          }
        } finally {

        }
      }
      jobThreads.clear();
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

  private Thread startJob(BackgroundJob job) {
    Thread background = new Thread(getSubject().associateWith(job));
    log.info("Starting job [{}]: {}", job.getName(), job.getDescription());
    background.setPriority(job.getPriority());
    background.start();
    return background;
  }

  private Subject getSubject() {
    // Login as background job user
    try {
      PrincipalCollection principals = SecurityUtils.getSecurityManager()
          .authenticate(new BackgroundJobServiceAuthToken()).getPrincipals();
      return new Subject.Builder().principals(principals).authenticated(true).buildSubject();
    } catch(AuthenticationException e) {
      log.warn("Failed to obtain system user credentials: {}", e.getMessage());
      throw new RuntimeException(e);
    }
  }

}
