/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.server.httpd;

import org.obiba.opal.core.service.UnitKeyStoreService;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.core.unit.UnitKeyStore;
import org.obiba.opal.server.httpd.ssl.FunctionalUnitSslContextFactory;
import org.obiba.opal.server.rest.OpalApplication;
import org.obiba.opal.server.rest.OpalApplicationFactory;
import org.obiba.opal.shell.commands.AbstractOpalRuntimeDependentCommand;
import org.obiba.opal.shell.commands.CommandUsage;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
@CommandUsage(description = "Grant permissions to functional units.", syntax = "Syntax: grant --unit NAME --perm PERM TABLES...")
public class GrantCommand extends AbstractOpalRuntimeDependentCommand<GrantCommandOptions> {

  @Autowired
  private OpalApplicationFactory applicationFactory;

  @Autowired
  private OpalHttpServer httpServer;

  @Autowired
  private UnitKeyStoreService keystoreService;

  @Override
  public void execute() {
    OpalApplication application = applicationFactory.createApplication(httpServer.getComponent().getContext().createChildContext());
    for(String table : options.getTables()) {
      application.getContext().getParameters().add("table", table);
    }

    UnitKeyStore opalKeyStore = keystoreService.getUnitKeyStore(FunctionalUnit.OPAL_INSTANCE);
    UnitKeyStore unitKeyStore = keystoreService.getUnitKeyStore(options.getUnit());

    FunctionalUnitSslContextFactory fussl = new FunctionalUnitSslContextFactory(opalKeyStore, unitKeyStore);
    try {
      httpServer.addApplication(fussl, 8443, application);
      getShell().printf("HTTPS server started.\n");
    } catch(Exception e) {
      getShell().printf("Unable to start HTTPS server: %s\n", e.getMessage());
      throw new RuntimeException(e);
    }
  }
}
