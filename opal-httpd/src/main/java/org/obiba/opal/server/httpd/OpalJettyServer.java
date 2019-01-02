/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.server.httpd;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import io.buji.pac4j.filter.SecurityFilter;
import org.apache.shiro.web.env.EnvironmentLoader;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.security.ConstraintAware;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.AllowSymLinkAliasChecker;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlets.GzipFilter;
import org.eclipse.jetty.util.resource.PathResource;
import org.eclipse.jetty.util.security.Constraint;
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

import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Properties;

import static javax.servlet.DispatcherType.*;
import static org.springframework.web.context.ContextLoader.CONFIG_LOCATION_PARAM;

/**
 *
 */

public class OpalJettyServer {

  private static final Logger log = LoggerFactory.getLogger(OpalJettyServer.class);

  private static final String MAX_IDLE_TIME = "30000";

  private static final int REQUEST_HEADER_SIZE = 8192;

  private static final String MAX_FORM_CONTENT_SIZE = "200000";

  private static String[] GZIP_MIME_TYPES = { "text/css", "text/html", "text/plain", "text/csv",
      "application/xml", "application/json", "application/x-protobuf+json", "application/javascript" };

  private Server jettyServer;

  private ServletContextHandler servletContextHandler;

  private String httpPort;

  private String httpsPort;

  public void start() throws Exception {
    init();
    log.info("Starting Opal HTTP/s Server on ports {}/{}", httpPort, httpsPort);
    jettyServer.start();
  }

  public void stop() throws Exception {
    jettyServer.stop();
  }

  private void init() throws IOException, URISyntaxException {
    // LOGJAM
    System.setProperty("jdk.tls.ephemeralDHKeySize", "2048");
    jettyServer = new Server();
    // OPAL-342: We will manually stop the Jetty server instead of relying its shutdown hook
    jettyServer.setStopAtShutdown(false);

    Properties properties = loadProperties();
    httpPort = properties.getProperty("org.obiba.opal.http.port");
    httpsPort = properties.getProperty("org.obiba.opal.https.port");
    int maxIdleTime = Integer.valueOf(properties.getProperty("org.obiba.opal.maxIdleTime", MAX_IDLE_TIME));

    // OPAL-2687
    String excludedProtocols = properties.getProperty("org.obiba.opal.ssl.excludedProtocols");

    // OPAL-2752
    String includedCipherSuites = properties.getProperty("org.obiba.opal.ssl.includedCipherSuites");

    configureHttpConnector(httpPort == null ? null : Integer.valueOf(httpPort), createHttpConfiguration(maxIdleTime));
    configureSslConnector(httpsPort == null ? null : Integer.valueOf(httpsPort), createHttpConfiguration(maxIdleTime),
        excludedProtocols, includedCipherSuites);

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

  private void configureHttpConnector(@Nullable Integer httpPort, HttpConfiguration httpConfig) {
    if(httpPort == null || httpPort <= 0) return;
    ServerConnector httpConnector = new ServerConnector(jettyServer, new HttpConnectionFactory(httpConfig));
    httpConnector.setPort(httpPort);
    jettyServer.addConnector(httpConnector);
  }

  private void configureSslConnector(@Nullable Integer httpsPort, HttpConfiguration httpConfig, String excludedProtocols,
      String includedCipherSuites) {
    if(httpsPort == null || httpsPort <= 0) return;
    httpConfig.setSecureScheme("https");
    httpConfig.setSecurePort(httpsPort);
    httpConfig.addCustomizer(new SecureRequestCustomizer());
    ServerConnector sslConnector = new ServerConnector(jettyServer,
        new SslConnectionFactory(createSslContext(excludedProtocols, includedCipherSuites), HttpVersion.HTTP_1_1.asString()),
        new HttpConnectionFactory(httpConfig));
    sslConnector.setPort(httpsPort);
    jettyServer.addConnector(sslConnector);
  }

  private HttpConfiguration createHttpConfiguration(int maxIdleTime) {
    HttpConfiguration httpConfig = new HttpConfiguration();
    httpConfig.setSendServerVersion(false);
    httpConfig.setRequestHeaderSize(REQUEST_HEADER_SIZE);
    httpConfig.setIdleTimeout(maxIdleTime);
    return httpConfig;
  }

  private SslContextFactory createSslContext(String excludedProtocols, String includedCipherSuites) {
    SslContextFactory jettySsl = new SslContextFactory() {

      @Override
      protected void doStart() throws Exception {
        org.obiba.ssl.SslContextFactory sslContextFactory = WebApplicationContextUtils
            .getRequiredWebApplicationContext(servletContextHandler.getServletContext())
            .getBean(org.obiba.ssl.SslContextFactory.class);
        setSslContext(sslContextFactory.createSslContext());
        super.doStart();
      }

    };
    jettySsl.setWantClientAuth(true);
    jettySsl.setNeedClientAuth(false);
    jettySsl.setRenegotiationAllowed(false);

    if(!Strings.isNullOrEmpty(excludedProtocols)) {
      String[] protocols = excludedProtocols.split("\\s*,\\s*");
      if(protocols.length > 0) jettySsl.addExcludeProtocols(protocols);
    }

    if(!Strings.isNullOrEmpty(includedCipherSuites)) {
      String[] ciphers = includedCipherSuites.split("\\s*,\\s*");
      if(ciphers.length > 0) jettySsl.setIncludeCipherSuites(ciphers);
    }

    return jettySsl;
  }

  private Handler createServletHandler() {
    servletContextHandler = new ServletContextHandler(ServletContextHandler.SECURITY);
    servletContextHandler.setContextPath("/");
    servletContextHandler.addAliasCheck(new AllowSymLinkAliasChecker());

    initEventListeners();
    initNotAllowedMethods();
    initFilters();

    servletContextHandler.setInitParameter(CONFIG_LOCATION_PARAM, "classpath:/META-INF/spring/opal-server/context.xml");
    servletContextHandler.setInitParameter("resteasy.servlet.mapping.prefix", "/ws");
    //servletContextHandler.setInitParameter(EnvironmentLoader.ENVIRONMENT_CLASS_PARAM, "org.obiba.opal.core.service.security.OpalIniEnvironment");
    //servletContextHandler.setInitParameter(EnvironmentLoader.CONFIG_LOCATIONS_PARAM, System.getProperty("OPAL_HOME") + "/conf/shiro.ini");
    servletContextHandler.addServlet(HttpServletDispatcher.class, "/ws/*");

    GzipHandler gzipHandler = new GzipHandler();
    gzipHandler.setHandler(servletContextHandler);
    gzipHandler.setIncludedMimeTypes(GZIP_MIME_TYPES);

    return gzipHandler;
  }

  private void initEventListeners() {
    servletContextHandler.addEventListener(new ResteasyBootstrap());
    servletContextHandler.addEventListener(new Spring4ContextLoaderListener());
    servletContextHandler.addEventListener(new RequestContextListener());
    //servletContextHandler.addEventListener(new OpalEnvironmentLoaderListener());
  }

  private void initFilters() {
    servletContextHandler.addFilter(OpalVersionFilter.class, "/*", EnumSet.of(REQUEST));
    initPac4jFilter();
    FilterHolder authenticationFilterHolder = new FilterHolder(DelegatingFilterProxy.class);
    authenticationFilterHolder.setName("authenticationFilter");
    authenticationFilterHolder.setInitParameters(ImmutableMap.of("targetFilterLifecycle", "true"));
    servletContextHandler.addFilter(authenticationFilterHolder, "/ws/*", EnumSet.of(REQUEST, FORWARD, INCLUDE, ERROR));

    servletContextHandler.addFilter(GzipFilter.class, "/*", EnumSet.of(REQUEST));
  }

  private void initPac4jFilter() {
    servletContextHandler.addFilter(OpalSecurityFilter.Wrapper.class, "/ws/*", EnumSet.of(REQUEST, FORWARD, INCLUDE, ERROR));
    servletContextHandler.addFilter(OpalCallbackFilter.Wrapper.class, "/callback", EnumSet.of(REQUEST, FORWARD, INCLUDE, ERROR));
  }

  private void initNotAllowedMethods() {
    ConstraintAware securityHandler = (ConstraintAware) servletContextHandler.getSecurityHandler();
    securityHandler.addConstraintMapping(newMethodConstraintMapping("TRACE"));
    securityHandler.addConstraintMapping(newMethodConstraintMapping("TRACK"));
  }

  private ConstraintMapping newMethodConstraintMapping(String method) {
    Constraint constraint = new Constraint();
    constraint.setName("Disable " + method);
    constraint.setAuthenticate(true);

    ConstraintMapping mapping = new ConstraintMapping();
    mapping.setConstraint(constraint);
    mapping.setMethod(method);
    mapping.setPathSpec("/");

    return mapping;
  }

  private Handler createDistFileHandler(String directory) throws IOException, URISyntaxException {
    return createFileHandler("file://" + resolveOpalDistPath() + directory);
  }

  private Handler createFileHandler(String fileUrl) throws IOException, URISyntaxException {
    log.info("Creating a file handler for the following URL : {}", fileUrl);
    ResourceHandler resourceHandler = new ResourceHandler();
    resourceHandler.setBaseResource(new PathResource(new URL(fileUrl)));
    resourceHandler.setRedirectWelcome(true);

    GzipHandler gzipHandler = new GzipHandler();
    gzipHandler.setHandler(resourceHandler);
    gzipHandler.setIncludedMimeTypes(GZIP_MIME_TYPES);

    return gzipHandler;
  }

  private Handler createExtensionFileHandler(String filePath) throws IOException, URISyntaxException {
    File file = new File(filePath);
    if(!file.exists() && !file.mkdirs()) {
      throw new RuntimeException("Cannot create extensions directory: " + file.getAbsolutePath());
    }
    return createFileHandler("file://" + filePath);
  }

  /**
   * Make sure there are no symbolic links in the Opal distribution folder path.
   * 
   * @return
   * @throws IOException
   */
  private String resolveOpalDistPath() throws IOException {
    return new File(System.getProperty("OPAL_DIST")).toPath().toRealPath().toString();
  }

  // https://issues.jboss.org/browse/RESTEASY-1012
  static class Spring4ContextLoaderListener extends ContextLoaderListener {

    private final SpringContextLoaderSupport springContextLoaderSupport = new SpringContextLoaderSupport();

    @Override
    protected void customizeContext(ServletContext servletContext,
        ConfigurableWebApplicationContext configurableWebApplicationContext) {
      super.customizeContext(servletContext, configurableWebApplicationContext);
      springContextLoaderSupport.customizeContext(servletContext, configurableWebApplicationContext);
    }
  }

}
