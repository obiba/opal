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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.annotation.PostConstruct;

import org.eclipse.jetty.ajp.Ajp13SocketConnector;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.FileResource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;
import org.jboss.resteasy.plugins.spring.SpringContextLoaderListener;
import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.core.runtime.NoSuchServiceConfigurationException;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.runtime.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 */

public class OpalJettyServer implements Service {

  private static final Logger log = LoggerFactory.getLogger(OpalJettyServer.class);

  private static final int MAX_IDLE_TIME = 30000;

  private static final int REQUEST_HEADER_SIZE = 8192;

  //  @Nullable
//  @Value("${org.obiba.opal.http.port}")
//  private Integer httpPort;
//
//  @Nullable
//  @Value("${org.obiba.opal.https.port}")
//  private Integer httpsPort;
//
//  @Nullable
//  @Value("${org.obiba.opal.ajp.port}")
//  private Integer ajpPort;
//
//  @Nullable
//  @Value("${org.obiba.opal.maxIdleTime}")
//  private Integer maxIdleTime;

  private Server jettyServer;

  private ServletContextHandler servletContextHandler;

  @PostConstruct
  public void init() {
    jettyServer = new Server();
    jettyServer.setSendServerVersion(false);
    // OPAL-342: We will manually stop the Jetty server instead of relying its shutdown hook
    jettyServer.setStopAtShutdown(false);

    configureHttpConnector();
    configureSslConnector();
    configureAjpConnector();

    HandlerList handlers = new HandlerList();

    // Add a file handler that points to the Opal GWT client directory
    handlers.addHandler(createDistFileHandler("/webapp"));
    // Add webapp extensions
    handlers.addHandler(createExtensionFileHandler(OpalRuntime.WEBAPP_EXTENSION));
    // Add a file handler that points to the Opal BIRT extension update-site
    handlers.addHandler(createDistFileHandler("/update-site"));
    handlers.addHandler(createServletHandler());
    jettyServer.setHandler(handlers);
  }

  private void configureAjpConnector() {
    if(ajpPort == null || ajpPort <= 0) return;
    Connector ajp = new Ajp13SocketConnector();
    ajp.setPort(ajpPort);
    jettyServer.addConnector(ajp);
  }

  private void configureHttpConnector() {
    if(httpPort == null || httpPort <= 0) return;

    Connector httpConnector = new SelectChannelConnector();
    httpConnector.setPort(httpPort);
    httpConnector.setMaxIdleTime(maxIdleTime == null ? MAX_IDLE_TIME : maxIdleTime);
    httpConnector.setRequestHeaderSize(REQUEST_HEADER_SIZE);
    jettyServer.addConnector(httpConnector);
  }

  private void configureSslConnector() {
    if(httpsPort == null || httpsPort <= 0) return;

    SslContextFactory jettySsl = new SslContextFactory() {

      @Override
      protected void doStart() throws Exception {

        org.obiba.opal.server.ssl.SslContextFactory sslContextFactory = WebApplicationContextUtils
            .getRequiredWebApplicationContext(servletContextHandler.getServletContext())
            .getBean(org.obiba.opal.server.ssl.SslContextFactory.class);
        setSslContext(sslContextFactory.createSslContext());
      }

      @Override
      public void checkKeyStore() {
      }
    };
    jettySsl.setWantClientAuth(true);
    jettySsl.setNeedClientAuth(false);

    Connector sslConnector = new SslSelectChannelConnector(jettySsl);
    sslConnector.setPort(httpsPort);
    sslConnector.setMaxIdleTime(maxIdleTime == null ? MAX_IDLE_TIME : maxIdleTime);
    sslConnector.setRequestHeaderSize(REQUEST_HEADER_SIZE);

    jettyServer.addConnector(sslConnector);
  }

  @Bean
  public ServletContextHandler getServletContextHandler() {
    return servletContextHandler;
  }

  @Override
  public boolean isRunning() {
    return jettyServer.isRunning();
  }

  @Override
  public void start() {
    try {
      log.info("Starting Opal HTTP/s Server on port {}", jettyServer.getConnectors()[0].getPort());
      jettyServer.start();
    } catch(Exception e) {
      log.error("Error starting jetty", e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public void stop() {
    try {
      jettyServer.stop();
    } catch(Exception e) {
      // log and ignore
      log.warn("Exception during HTTPd server shutdown", e);
    }

  }

  @Override
  public String getName() {
    return "jetty";
  }

  @Override
  public OpalConfigurationExtension getConfig() throws NoSuchServiceConfigurationException {
    throw new NoSuchServiceConfigurationException(getName());
  }

  private Handler createServletHandler() {
    servletContextHandler = new ServletContextHandler(ServletContextHandler.NO_SECURITY);
    servletContextHandler.setContextPath("/");
    servletContextHandler.addEventListener(new ResteasyBootstrap());
    servletContextHandler.addEventListener(new SpringContextLoaderListener());
    servletContextHandler.addEventListener(new RequestContextListener());
    servletContextHandler.addFilter(new FilterHolder(new OpalVersionFilter()), "/*", FilterMapping.DEFAULT);
    servletContextHandler.addFilter(new FilterHolder(new AuthenticationFilter()), "/ws/*", FilterMapping.DEFAULT);
    //TODO fix application context xml
    servletContextHandler.setInitParameter(ContextLoader.CONFIG_LOCATION_PARAM, "classpath:application-context.xml");
    servletContextHandler.addServlet(new ServletHolder(new HttpServletDispatcher()), "/ws/*");
    return servletContextHandler;
  }

  private Handler createDistFileHandler(String directory) {
    return createFileHandler("file://" + System.getProperty("OPAL_DIST") + directory);
  }

  private Handler createExtensionFileHandler(String filePath) {
    File file = new File(filePath);
    if(!file.exists()) {
      if(!file.mkdirs()) {
        throw new RuntimeException("Cannot create extensions directory: " + file.getAbsolutePath());
      }
    }
    return createFileHandler("file://" + filePath);
  }

  private Handler createFileHandler(String fileUrl) {
    ResourceHandler resourceHandler = new ResourceHandler();
    try {
      resourceHandler.setBaseResource(new FileResource(new URL(fileUrl)));
      resourceHandler.setAliases(true);
      log.info("Created a file handler for the following URL : {}", fileUrl);
    } catch(MalformedURLException e) {
      throw new RuntimeException(e);
    } catch(IOException e) {
      throw new RuntimeException(e);
    } catch(URISyntaxException e) {
      throw new RuntimeException(e);
    }
    return resourceHandler;
  }

}
