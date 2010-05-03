/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.client.rest;

import java.util.Collections;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.obiba.magma.MagmaEngine;
import org.obiba.opal.server.ssl.SslContextFactory;
import org.obiba.opal.shell.commands.AbstractOpalRuntimeDependentCommand;
import org.obiba.opal.shell.commands.CommandUsage;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.util.Series;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
@CommandUsage(description = "Connect to a functional unit's hosted tables.", syntax = "Syntax: connect --unit NAME --url url")
public class ConnectCommand extends AbstractOpalRuntimeDependentCommand<ConnectCommandOptions> {

  @Autowired
  private SslContextFactory sslContextFactory;

  @Override
  public void execute() {
    try {
      Context httpsCtx = new Context();
      httpsCtx.getAttributes().put("sslContextFactory", new org.restlet.engine.security.SslContextFactory() {

        @Override
        public void init(Series<Parameter> parameters) {
        }

        @Override
        public SSLContext createSslContext() throws Exception {
          return sslContextFactory.createSslContext();
        }
      });
      httpsCtx.getParameters().add("hostnameVerifier", AllowAllHostnameVerifier.class.getName());

      Client client = new Client(httpsCtx, Collections.singletonList(Protocol.HTTPS), ExtendedHttpClientHelper.class.getName());

      RestDatasource ds = new RestDatasource(options.getUnit(), new Reference(options.getUrl()), client);
      MagmaEngine.get().addDatasource(ds);
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }
}
