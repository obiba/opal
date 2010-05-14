/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.server.rest.cfg;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.spi.Registry;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResteasyServletConfiguration {

  @Autowired
  private ServletContextHandler servletContextHandler;

  @Autowired
  private ResteasyDeployment resteasyDeployment;

  @PostConstruct
  public void configureResteasyServlet() {
    ServletContext servletContext = servletContextHandler.getServletContext();
    servletContext.setAttribute(ResteasyProviderFactory.class.getName(), resteasyDeployment.getProviderFactory());
    servletContext.setAttribute(Dispatcher.class.getName(), resteasyDeployment.getDispatcher());
    servletContext.setAttribute(Registry.class.getName(), resteasyDeployment.getRegistry());
    servletContextHandler.addServlet(new ServletHolder(new HttpServletDispatcher()), "/ws/*");
  }

}
