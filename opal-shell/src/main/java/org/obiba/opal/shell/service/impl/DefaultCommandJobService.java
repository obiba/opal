/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.shell.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

import org.obiba.opal.audit.OpalUserProvider;
import org.obiba.opal.shell.CommandJob;
import org.obiba.opal.shell.service.CommandJobService;

/**
 * Default implementation of {@link CommandJobService}.
 */
public class DefaultCommandJobService implements CommandJobService {
  //
  // Instance Variables
  //

  private OpalUserProvider userProvider;

  private Executor executor;

  private boolean isRunning;

  private List<CommandJob> history;

  //
  // Constructors
  //

  public DefaultCommandJobService() {
    history = new ArrayList<CommandJob>();
  }

  //
  // Service Methods
  //

  public void start() {
    isRunning = true;
  }

  public void stop() {
    isRunning = false;
  }

  public boolean isRunning() {
    return isRunning;
  }

  //
  // CommandJobService Methods
  //

  public void launchCommand(CommandJob commandJob) {
    commandJob.setOwner(userProvider.getUsername());
    commandJob.setSubmitTime(getCurrentTime());

    executor.execute(commandJob);

    history.add(0, commandJob);
  }

  public List<CommandJob> getHistory() {
    return Collections.unmodifiableList(history);
  }

  //
  // Methods
  //

  public void setUserProvider(OpalUserProvider userProvider) {
    this.userProvider = userProvider;
  }

  protected Date getCurrentTime() {
    return new Date();
  }
}
