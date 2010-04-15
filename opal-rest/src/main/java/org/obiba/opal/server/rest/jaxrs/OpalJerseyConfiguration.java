/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.server.rest.jaxrs;

import javax.annotation.PostConstruct;

import org.eclipse.jetty.servlet.ServletHolder;
import org.obiba.opal.server.httpd.OpalJettyServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;

@Configuration
public class OpalJerseyConfiguration {

  @Autowired
  private OpalJettyServer jettyServer;

  @PostConstruct
  public void addDispatcherServlet() {
    SpringServlet s = new SpringServlet() {
      @Override
      protected void initiate(ResourceConfig rc, WebApplication wa) {
        rc.getClasses().add(TableResource.class);
        rc.getClasses().add(VariableResource.class);
        super.initiate(rc, wa);
      }
    };
    jettyServer.getContext().addServlet(new ServletHolder(s), "/jersey/*");
  }
}
