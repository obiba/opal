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
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.EnumSet;
import java.util.Properties;

import javax.annotation.Nullable;
import javax.servlet.ServletContext;

import org.eclipse.jetty.ajp.Ajp13SocketConnector;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.resource.FileResource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;
import org.jboss.resteasy.plugins.spring.SpringContextLoaderSupport;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.DelegatingFilterProxy;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import static javax.servlet.DispatcherType.ERROR;
import static javax.servlet.DispatcherType.FORWARD;
import static javax.servlet.DispatcherType.INCLUDE;
import static javax.servlet.DispatcherType.REQUEST;
import static org.springframework.web.context.ContextLoader.CONFIG_LOCATION_PARAM;

/**
 *
 */

public class OpalJettyServer {

  private static final Logger log = LoggerFactory.getLogger(OpalJettyServer.class);

  private static final String MAX_IDLE_TIME = "30000";

  private static final int REQUEST_HEADER_SIZE = 8192;

  private static final String MAX_FORM_CONTENT_SIZE = "200000";

  private Server jettyServer;

  private ServletContextHandler servletContextHandler;

  public void start() throws Exception {
    init();
    log.info("Starting Opal HTTP/s Server on port {}", jettyServer.getConnectors()[0].getPort());
    jettyServer.start();
  }

  public void stop() throws Exception {
    jettyServer.stop();
  }

  private void init() throws IOException, URISyntaxException {
    jettyServer = new Server();
    jettyServer.setSendServerVersion(false);
    // OPAL-342: We will manually stop the Jetty server instead of relying its shutdown hook
    jettyServer.setStopAtShutdown(false);

    Properties properties = loadProperties();
    String httpPort = properties.getProperty("org.obiba.opal.http.port");
    String httpsPort = properties.getProperty("org.obiba.opal.https.port");
    String ajpPort = properties.getProperty("org.obiba.opal.ajp.port");
    int maxIdleTime = Integer.valueOf(properties.getProperty("org.obiba.opal.maxIdleTime", MAX_IDLE_TIME));

    // OPAL-2687
    String excludedProtocols = properties.getProperty("org.obiba.opal.ssl.excludedProtocols");

    configureHttpConnector(httpPort == null ? null : Integer.valueOf(httpPort), maxIdleTime);
    configureSslConnector(httpsPort == null ? null : Integer.valueOf(httpsPort), maxIdleTime, excludedProtocols);
    configureAjpConnector(ajpPort == null ? null : Integer.valueOf(ajpPort));

    // OPAL-2652
    int maxFormContentSize = Integer
        .valueOf(properties.getProperty("org.obiba.opal.maxFormContentSize", MAX_FORM_CONTENT_SIZE));
    jettyServer.setAttribute("org.eclipse.jetty.server.Request.maxFormContentSize", maxFormContentSize);

    HandlerList handlers = new HandlerList();

    // Add a file handler that points to the Opal GWT client directory
    handlers.addHandler(createDistFileHandler("/webapp"));
    // Add webapp extensions
    handlers.addHandler(createExtensionFileHandler(OpalRuntime.WEBAPP_EXTENSION));
    handlers.addHandler(createServletHandler());
    jettyServer.setHandler(handlers);
  }

  private Properties loadProperties() throws IOException {
    // ${OPAL_HOME}/conf/opal-config.properties
    try(FileInputStream inputStream = new FileInputStream(
        new File(System.getProperty("OPAL_HOME") + "/conf/opal-config.properties"))) {
      Properties properties = new Properties(PropertiesLoaderUtils.loadAllProperties("META-INF/defaults.properties"));
      properties.load(inputStream);
      return properties;
    }
  }

  private void configureAjpConnector(@Nullable Integer ajpPort) {
    if(ajpPort == null || ajpPort <= 0) return;
    Connector ajp = new Ajp13SocketConnector();
    ajp.setPort(ajpPort);
    jettyServer.addConnector(ajp);
  }

  private void configureHttpConnector(@Nullable Integer httpPort, int maxIdleTime) {
    if(httpPort == null || httpPort <= 0) return;

    Connector httpConnector = new SelectChannelConnector();
    httpConnector.setPort(httpPort);
    httpConnector.setMaxIdleTime(maxIdleTime);
    httpConnector.setRequestHeaderSize(REQUEST_HEADER_SIZE);
    jettyServer.addConnector(httpConnector);
  }

  private void configureSslConnector(@Nullable Integer httpsPort, int maxIdleTime, String excludedProtocols) {
    if(httpsPort == null || httpsPort <= 0) return;

    SslContextFactory jettySsl = new SslContextFactory() {

      @Override
      protected void doStart() throws Exception {
        org.obiba.ssl.SslContextFactory sslContextFactory = WebApplicationContextUtils
            .getRequiredWebApplicationContext(servletContextHandler.getServletContext())
            .getBean(org.obiba.ssl.SslContextFactory.class);
        setSslContext(sslContextFactory.createSslContext());
      }

      @Override
      public void checkKeyStore() {
      }
    };

    if(!Strings.isNullOrEmpty(excludedProtocols)) {
      String[] protocols = excludedProtocols.split("\\s*,\\s*");
      if(protocols.length > 0) jettySsl.addExcludeProtocols(protocols);
    }

    jettySsl.setWantClientAuth(true);
    jettySsl.setNeedClientAuth(false);

    Connector sslConnector = new SslSelectChannelConnector(jettySsl);
    sslConnector.setPort(httpsPort);
    sslConnector.setMaxIdleTime(maxIdleTime);
    sslConnector.setRequestHeaderSize(REQUEST_HEADER_SIZE);

    jettyServer.addConnector(sslConnector);
  }

  private Handler createServletHandler() {
    servletContextHandler = new ServletContextHandler(ServletContextHandler.NO_SECURITY);
    servletContextHandler.setContextPath("/");

    servletContextHandler.addEventListener(new ResteasyBootstrap());
    servletContextHandler.addEventListener(new Spring4ContextLoaderListener());
    servletContextHandler.addEventListener(new RequestContextListener());

    servletContextHandler.addFilter(OpalVersionFilter.class, "/*", EnumSet.of(REQUEST));
    FilterHolder authenticationFilterHolder = new FilterHolder(DelegatingFilterProxy.class);
    authenticationFilterHolder.setName("authenticationFilter");
    authenticationFilterHolder.setInitParameters(ImmutableMap.of("targetFilterLifecycle", "true"));
    servletContextHandler.addFilter(authenticationFilterHolder, "/ws/*", EnumSet.of(REQUEST, FORWARD, INCLUDE, ERROR));

    servletContextHandler.setInitParameter(CONFIG_LOCATION_PARAM, "classpath:/META-INF/spring/opal-server/context.xml");
    servletContextHandler.setInitParameter("resteasy.servlet.mapping.prefix", "/ws");
    servletContextHandler.addServlet(HttpServletDispatcher.class, "/ws/*");
    return servletContextHandler;
  }

  private Handler createDistFileHandler(String directory) throws IOException, URISyntaxException {
    return createFileHandler("file://" + System.getProperty("OPAL_DIST") + directory);
  }

  private Handler createExtensionFileHandler(String filePath) throws IOException, URISyntaxException {
    File file = new File(filePath);
    if(!file.exists() && !file.mkdirs()) {
      throw new RuntimeException("Cannot create extensions directory: " + file.getAbsolutePath());
    }
    return createFileHandler("file://" + filePath);
  }

  private Handler createFileHandler(String fileUrl) throws IOException, URISyntaxException {
    log.info("Creating a file handler for the following URL : {}", fileUrl);
    ResourceHandler resourceHandler = new ResourceHandler();
    resourceHandler.setBaseResource(new FileResource(new URL(fileUrl)));
    resourceHandler.setAliases(true);
    return resourceHandler;
  }

  // https://issues.jboss.org/browse/RESTEASY-1012
  public static class Spring4ContextLoaderListener extends ContextLoaderListener {

    private final SpringContextLoaderSupport springContextLoaderSupport = new SpringContextLoaderSupport();

    @Override
    protected void customizeContext(ServletContext servletContext,
        ConfigurableWebApplicationContext configurableWebApplicationContext) {
      super.customizeContext(servletContext, configurableWebApplicationContext);
      springContextLoaderSupport.customizeContext(servletContext, configurableWebApplicationContext);
    }
  }

}
