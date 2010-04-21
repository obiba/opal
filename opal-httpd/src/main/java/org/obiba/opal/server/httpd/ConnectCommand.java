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

import java.util.Collections;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.obiba.opal.core.service.UnitKeyStoreService;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.core.unit.UnitKeyStore;
import org.obiba.opal.server.httpd.ssl.FunctionalUnitSslContextFactory;
import org.obiba.opal.shell.commands.AbstractOpalRuntimeDependentCommand;
import org.obiba.opal.shell.commands.CommandUsage;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Protocol;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
@CommandUsage(description = "Connect to a functional unit's hosted tables.", syntax = "Syntax: connect --unit NAME --url url")
public class ConnectCommand extends AbstractOpalRuntimeDependentCommand<ConnectCommandOptions> {

  @Autowired
  private OpalHttpServer httpServer;

  @Autowired
  private UnitKeyStoreService keystoreService;

  @Override
  public void execute() {
    UnitKeyStore opalKeyStore = keystoreService.getUnitKeyStore(FunctionalUnit.OPAL_INSTANCE);
    UnitKeyStore unitKeyStore = keystoreService.getUnitKeyStore(options.getUnit());

    FunctionalUnitSslContextFactory fussl = new FunctionalUnitSslContextFactory(opalKeyStore, unitKeyStore);
    try {
      Context httpsCtx = httpServer.getComponent().getContext().createChildContext();
      httpsCtx.getAttributes().put("sslContextFactory", fussl);
      httpsCtx.getParameters().add("hostnameVerifier", AllowAllHostnameVerifier.class.getName());

      Client client = new Client(httpsCtx, Collections.singletonList(Protocol.HTTPS), ExtendedHttpClientHelper.class.getName());

      // TODO: commented due to code refactoring. Uncomment and fix once package structure is stable.
      // RestDatasource ds = new RestDatasource(options.getUnit(), new Reference(options.getUrl()), client);
      // MagmaEngine.get().addDatasource(ds);
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }
}
