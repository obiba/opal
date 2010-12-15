/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.server;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.obiba.opal.server.OpalServer.OpalServerOptions;

import uk.co.flamingpenguin.jewel.cli.CliFactory;

public class OpalDaemon implements Daemon {

  private OpalServerOptions opalServerOptions;

  private OpalServer opalServer;

  @Override
  public void destroy() {

  }

  @Override
  public void init(DaemonContext context) throws Exception {
    this.opalServerOptions = CliFactory.parseArguments(OpalServerOptions.class, context.getArguments());
  }

  @Override
  public void start() throws Exception {
    this.opalServer = new OpalServer(opalServerOptions);
    this.opalServer.boot();
  }

  @Override
  public void stop() throws Exception {
    this.opalServer.shutdown();
  }

}
