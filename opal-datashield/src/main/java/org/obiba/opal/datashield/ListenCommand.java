/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.datashield;

import org.obiba.opal.datashield.rest.LogoutResource;
import org.obiba.opal.datashield.rest.RResource;
import org.obiba.opal.server.httpd.OpalHttpServer;
import org.obiba.opal.server.rest.OpalApplication;
import org.obiba.opal.shell.commands.AbstractOpalRuntimeDependentCommand;
import org.obiba.opal.shell.commands.CommandUsage;
import org.restlet.ext.jaxrs.JaxRsApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@CommandUsage(description = "Listens for connections from a remote functional unit")
public class ListenCommand extends AbstractOpalRuntimeDependentCommand<ListenCommandOptions> {

  private static final Logger log = LoggerFactory.getLogger(ListenCommand.class);

  @Autowired
  private OpalHttpServer httpServer;

  public void execute() {
    OpalApplication app = new OpalApplication();
    app.getClasses().add(RResource.class);
    app.getClasses().add(LogoutResource.class);
    JaxRsApplication application = new JaxRsApplication();
    application.add(app);
    httpServer.addApplication(options.getRoot(), application);
  }
}
