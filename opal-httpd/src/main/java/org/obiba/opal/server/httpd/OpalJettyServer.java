/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.server.httpd;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import jakarta.annotation.Nullable;
import jakarta.servlet.ServletContext;
import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.FilterHolder;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.ee10.servlet.security.ConstraintAware;
import org.eclipse.jetty.ee10.servlet.security.ConstraintMapping;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.pathmap.PathSpec;
import org.eclipse.jetty.security.Constraint;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.CrossOriginHandler;
import org.eclipse.jetty.server.handler.PathMappingsHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.eclipse.jetty.util.resource.Resources;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;
import org.jboss.resteasy.plugins.spring.SpringContextLoaderSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.DelegatingFilterProxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static jakarta.servlet.DispatcherType.*;
import static org.springframework.web.context.ContextLoader.CONFIG_LOCATION_PARAM;

public class OpalJettyServer {

  private static final Logger log = LoggerFactory.getLogger(OpalJettyServer.class);

  private static final String MAX_IDLE_TIME = "1800000";

  private static final int REQUEST_HEADER_SIZE = 8192;

  private static final String MAX_FORM_CONTENT_SIZE = "200000";

  private static String[] GZIP_MIME_TYPES = {"text/css", "text/html", "text/plain", "text/csv", "text/javascript",
      "application/xml", "application/json", "application/x-protobuf+json", "application/javascript"};

  private Server jettyServer;

  private ServletContextHandler servletContextHandler;

  private String httpPort;

  private String httpsPort;

  private String contextPath = "/";

  private boolean productionMode = true;

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
    productionMode = Boolean.valueOf(properties.getProperty("productionMode", "true"));
    httpPort = properties.getProperty("org.obiba.opal.http.port");
    httpsPort = properties.getProperty("org.obiba.opal.https.port");
    int maxIdleTime = Integer.valueOf(properties.getProperty("org.obiba.opal.maxIdleTime", MAX_IDLE_TIME));

    contextPath = properties.getProperty("org.obiba.opal.server.context-path", "");
    if (!Strings.isNullOrEmpty(contextPath) && !contextPath.startsWith("/") || contextPath.endsWith("/")) {
      throw new IllegalArgumentException("ContextPath must start with '/' and not end with '/'");
    }
    if (Strings.isNullOrEmpty(contextPath)) contextPath = "/";

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

    PathMappingsHandler pathMappingsHandler = new PathMappingsHandler();
    Handler servletHandler = createServletHandler(properties);
    String prefix = contextPath.equals("/") ? "" : contextPath;
    pathMappingsHandler.addMapping(PathSpec.from(prefix + "/ws/*"), servletHandler);
    pathMappingsHandler.addMapping(PathSpec.from(prefix + "/auth/*"), servletHandler);
    pathMappingsHandler.addMapping(PathSpec.from(prefix + "/*"), createDistFileHandler("webapp"));

    //pathMappingsHandler.dumpStdErr();

    jettyServer.setHandler(pathMappingsHandler);
  }

  private Properties loadProperties() throws IOException {
    // ${OPAL_HOME}/conf/opal-config.properties
    try (FileInputStream inputStream = new FileInputStream(
        new File(System.getProperty("OPAL_HOME") + "/conf/opal-config.properties"))) {
      Properties properties = new Properties(PropertiesLoaderUtils.loadAllProperties("META-INF/defaults.properties"));
      properties.load(inputStream);
      return properties;
    }
  }

  private void configureHttpConnector(@Nullable Integer httpPort, HttpConfiguration httpConfig) {
    if (httpPort == null || httpPort <= 0) return;
    ServerConnector httpConnector = new ServerConnector(jettyServer, new HttpConnectionFactory(httpConfig));
    httpConnector.setPort(httpPort);
    jettyServer.addConnector(httpConnector);
  }

  private void configureSslConnector(@Nullable Integer httpsPort, HttpConfiguration httpConfig, String excludedProtocols,
                                     String includedCipherSuites) {
    if (httpsPort == null || httpsPort <= 0) return;
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
    if (!productionMode) {
      SecureRequestCustomizer customizer = new SecureRequestCustomizer();
      customizer.setSniHostCheck(false);
      httpConfig.addCustomizer(customizer);
    }
    return httpConfig;
  }

  private SslContextFactory.Server createSslContext(String excludedProtocols, String includedCipherSuites) {
    SslContextFactory.Server jettySsl = new SslContextFactory.Server() {

      @Override
      protected void doStart() throws Exception {
        org.obiba.ssl.SslContextFactory sslContextFactory = WebApplicationContextUtils
            .getRequiredWebApplicationContext(servletContextHandler.getServletContext())
            .getBean(org.obiba.ssl.SslContextFactory.class);
        setSslContext(sslContextFactory.createSslContext());
        super.doStart();
      }
    };
    jettySsl.setTrustAll(true);
    jettySsl.setWantClientAuth(true);
    jettySsl.setNeedClientAuth(false);
    jettySsl.setRenegotiationAllowed(false);

    if (!Strings.isNullOrEmpty(excludedProtocols)) {
      String[] protocols = excludedProtocols.split("\\s*,\\s*");
      if (protocols.length > 0) jettySsl.addExcludeProtocols(protocols);
    }

    if (!Strings.isNullOrEmpty(includedCipherSuites)) {
      String[] ciphers = includedCipherSuites.split("\\s*,\\s*");
      if (ciphers.length > 0) jettySsl.setIncludeCipherSuites(ciphers);
    }

    return jettySsl;
  }

  private Handler createServletHandler(Properties properties) throws IOException, URISyntaxException {
    servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS | ServletContextHandler.SECURITY);
    servletContextHandler.setContextPath(contextPath);
    servletContextHandler.getSessionHandler().setSessionCookie("JSESSIONID_" + ("-1".equals(httpPort) ? httpsPort : httpPort));
    servletContextHandler.addAliasCheck(new SymlinkAllowedResourceAliasChecker(servletContextHandler));

    servletContextHandler.getSessionHandler().setHttpOnly(true);
    servletContextHandler.getSessionHandler().setSecureRequestOnly(true);
    servletContextHandler.getSessionHandler().getSessionCookieConfig().setHttpOnly(true);
    servletContextHandler.getSessionHandler().getSessionCookieConfig().setSecure(true);

    initEventListeners();
    initNotAllowedMethods();
    initFilters(properties);

    servletContextHandler.setInitParameter(CONFIG_LOCATION_PARAM, "classpath:/META-INF/spring/opal-server/context.xml");
    servletContextHandler.setInitParameter("resteasy.servlet.mapping.prefix", "/ws");
    servletContextHandler.addServlet(HttpServletDispatcher.class, "/ws/*");

    GzipHandler gzipHandler = makeGzipHandler();
    gzipHandler.setHandler(servletContextHandler);
    Handler rootHandler = gzipHandler;

    String corsAllowed = properties.getProperty("cors.allowed");
    if (!Strings.isNullOrEmpty(corsAllowed)) {
      CrossOriginHandler corsHandler = makeCrossOriginHandler(Splitter.on(",").splitToStream(corsAllowed).collect(Collectors.toSet()));
      corsHandler.setHandler(rootHandler);
      rootHandler = corsHandler;
    }

    return rootHandler;
  }

  private CrossOriginHandler makeCrossOriginHandler(Set<String> origins) {
    CrossOriginHandler corsHandler = new CrossOriginHandler();
    corsHandler.setAllowedOriginPatterns(origins);
    corsHandler.setAllowedHeaders(Set.of("Content-Type", "Access-Control-Allow-Origin", "X-File-Key", "X-Opal-TOTP", "X-Obiba-TOTP"));
    corsHandler.setAllowedMethods(Set.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    corsHandler.setExposedHeaders(Set.of("X-Opal-Version", "Location", "X-TOTP", "Content-Disposition", "Allow", "WWW-Authenticate"));
    corsHandler.setAllowCredentials(true);
    return corsHandler;
  }

  private GzipHandler makeGzipHandler() {
    GzipHandler gzipHandler = new GzipHandler();
    gzipHandler.setIncludedMimeTypes(GZIP_MIME_TYPES);
    return gzipHandler;
  }

  private void initEventListeners() {
    servletContextHandler.addEventListener(new ResteasyBootstrap());
    servletContextHandler.addEventListener(new Spring4ContextLoaderListener());
    servletContextHandler.addEventListener(new RequestContextListener());
    servletContextHandler.addEventListener(new OpalEnvironmentLoaderListener());
  }

  private void initFilters(Properties properties) {
    servletContextHandler.addFilter(OpalVersionFilter.class, "/*", EnumSet.of(REQUEST));

    initOIDCFilter(properties);

    FilterHolder authenticationFilterHolder = new FilterHolder(DelegatingFilterProxy.class);
    authenticationFilterHolder.setName("authenticationFilter");
    authenticationFilterHolder.setInitParameters(ImmutableMap.of("targetFilterLifecycle", "true"));
    servletContextHandler.addFilter(authenticationFilterHolder, "/ws/*", EnumSet.of(REQUEST, FORWARD, INCLUDE, ERROR));
  }

  private void initOIDCFilter(Properties properties) {
    servletContextHandler.addFilter(OpalLoginFilter.Wrapper.class, "/auth/login/*", EnumSet.of(REQUEST, FORWARD, INCLUDE, ERROR));
    servletContextHandler.addFilter(OpalCallbackFilter.Wrapper.class, "/auth/callback/*", EnumSet.of(REQUEST, FORWARD, INCLUDE, ERROR));
  }

  private void initNotAllowedMethods() {
    ConstraintAware securityHandler = (ConstraintAware) servletContextHandler.getSecurityHandler();
    securityHandler.addConstraintMapping(newMethodConstraintMapping("TRACE"));
    securityHandler.addConstraintMapping(newMethodConstraintMapping("TRACK"));
    securityHandler.addConstraintMapping(newPathConstraintMapping("/WEB-INF"));
    securityHandler.addConstraintMapping(newPathConstraintMapping("/META-INF"));
  }

  private ConstraintMapping newMethodConstraintMapping(String method) {
    Constraint constraint = Constraint.from("Disable " + method, Constraint.Authorization.ANY_USER);

    ConstraintMapping mapping = new ConstraintMapping();
    mapping.setConstraint(constraint);
    mapping.setMethod(method);
    mapping.setPathSpec("/");

    return mapping;
  }

  private ConstraintMapping newPathConstraintMapping(String path) {
    Constraint constraint = Constraint.from("Disable GET " + path, Constraint.Authorization.FORBIDDEN);

    ConstraintMapping mapping = new ConstraintMapping();
    mapping.setConstraint(constraint);
    mapping.setMethod("GET");
    mapping.setPathSpec(path);

    return mapping;
  }

  private Handler.Singleton createDistFileHandler(String directory) throws IOException, URISyntaxException {
    return createFileHandler(resolveOpalDistURI() + directory);
  }

  private Handler.Singleton createFileHandler(String fileUrl) throws IOException, URISyntaxException {

    log.info("Creating a file handler for the following URL : {}", fileUrl);
    ResourceHandler resourceHandler = new ResourceHandler();
    Resource base = ResourceFactory.of(resourceHandler).newResource(new URI(fileUrl));
    if (!Resources.isReadableDirectory(base))
      throw new IOException("Resource is not a readable directory: " + fileUrl);
    resourceHandler.setBaseResource(base);
    resourceHandler.setDirAllowed(true);
    resourceHandler.setWelcomeFiles(List.of("index.html"));

    GzipHandler gzipHandler = makeGzipHandler();
    gzipHandler.setHandler(resourceHandler);

    ContextHandler ctxHandler = new ContextHandler(contextPath);
    ctxHandler.setHandler(gzipHandler);

    return ctxHandler;
  }

  /**
   * Make sure there are no symbolic links in the Opal distribution folder path.
   *
   * @return
   * @throws IOException
   */
  private String resolveOpalDistURI() throws IOException {
    String uri = new File(System.getProperty("OPAL_DIST")).toPath().toRealPath().toUri().toString();
    uri = normalizeFileURI(uri);
    if (!uri.endsWith("/")) uri = uri + "/";
    return uri;
  }

  private String normalizeFileURI(String uri) {
    if (uri.startsWith("file:///")) return uri;
    if (uri.startsWith("file://")) return uri.replaceFirst("file://", "file:///");
    if (uri.startsWith("file:/")) return uri.replaceFirst("file:/", "file:///");
    return "file:///" + uri;
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
