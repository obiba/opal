/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.server.rest;

import java.util.Collections;

import org.obiba.magma.ValueTable;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

/**
 *
 */
public class OpalApplication extends Application {

  @Autowired
  private PlatformTransactionManager txManager;

  public OpalApplication(Context context) {
    super(context);
  }

  @Override
  public Restlet createInboundRoot() {
    Router router = new Router();
    router.attach("/tables", TablesResource.class);
    router.attach("/table/{table}/variables", VariablesResource.class);
    router.attach("/table/{table}/entities", EntitiesResource.class);
    router.attach("/table/{table}/valueSet/{identifier}", ValueSetResource.class);

    // Executes the request within a transaction
    TransactionFilter tx = new TransactionFilter(txManager);
    tx.setNext(router);

    /*
     * // Authenticates / authorizes through Shiro ChallengeAuthenticator guard = new
     * ChallengeAuthenticator(application.getContext().createChildContext(), ChallengeScheme.HTTP_BASIC, "Opal");
     * ShiroVerifierAndEnroler shiro = new ShiroVerifierAndEnroler(); guard.setVerifier(shiro); guard.setEnroler(shiro);
     * guard.setNext(tx);
     */

    return tx;
  }

  public void addTable(String table) {
    addTables(Collections.singleton(table));
  }

  public void addTables(Iterable<String> tables) {
    for(String table : tables) {
      ValueTable vt = MagmaEngineTableResolver.valueOf(table).resolveTable();
      getContext().getParameters().add("table", vt.getName());
      getContext().getParameters().add(vt.getName(), vt.getDatasource().getName());
    }
  }
}
