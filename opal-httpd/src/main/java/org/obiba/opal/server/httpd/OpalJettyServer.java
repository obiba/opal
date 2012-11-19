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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.mgt.SecurityManager;
import org.eclipse.jetty.ajp.Ajp13SocketConnector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.resource.FileResource;
import org.obiba.opal.core.runtime.Service;
import org.obiba.opal.server.httpd.security.AuthenticationFilter;
import org.obiba.opal.server.ssl.SslContextFactory;
import org.obiba.runtime.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.filter.RequestContextFilter;

/**
 *
 */
@Component
public class OpalJettyServer implements Service {

  private static final Logger log = LoggerFactory.getLogger(OpalJettyServer.class);

  private final Server jettyServer;

  private final ServletContextHandler contextHandler;

  @Autowired
  private Version opalVersion;

  private ConfigurableApplicationContext webApplicationContext;

  @Autowired
  @SuppressWarnings({"unchecked", "PMD.ExcessiveParameterList"})
  public OpalJettyServer(final ApplicationContext ctx, final SecurityManager securityMgr,
      final SslContextFactory sslContextFactory, final PlatformTransactionManager txmgr,
      final @Value("${org.obiba.opal.http.port}") Integer httpPort,
      final @Value("${org.obiba.opal.https.port}") Integer httpsPort,
      final @Value("${org.obiba.opal.ajp.port}") Integer ajpPort,
      final @Value("${org.obiba.opal.maxIdleTime}") Integer maxIdleTime) {
    Server server = new Server();
    server.setSendServerVersion(false);
    // OPAL-342: We will manually stop the Jetty server instead of relying its shutdown hook
    server.setStopAtShutdown(false);

    if(httpPort != null && httpPort > 0) {
      SelectChannelConnector httpConnector = new SelectChannelConnector();
      httpConnector.setPort(httpPort);
      httpConnector.setMaxIdleTime(maxIdleTime != null ? maxIdleTime : 30000);
      httpConnector.setRequestHeaderSize(8192);
      server.addConnector(httpConnector);
    }

    if(httpsPort != null && httpsPort > 0) {

      org.eclipse.jetty.http.ssl.SslContextFactory jettySsl = new org.eclipse.jetty.http.ssl.SslContextFactory() {

        @Override
        protected void doStart() throws Exception {
          super.setSslContext(sslContextFactory.createSslContext());
        }

        @Override
        public boolean checkConfig() {
          return true;
        }
      };

      jettySsl.setWantClientAuth(true);
      jettySsl.setNeedClientAuth(false);

      SslSelectChannelConnector sslConnector = new SslSelectChannelConnector(jettySsl);
      sslConnector.setPort(httpsPort);
      sslConnector.setMaxIdleTime(maxIdleTime != null ? maxIdleTime : 30000);
      sslConnector.setRequestHeaderSize(8192);

      server.addConnector(sslConnector);
    }

    if(ajpPort != null && ajpPort > 0) {
      Ajp13SocketConnector ajp = new Ajp13SocketConnector();
      ajp.setPort(ajpPort);
      server.addConnector(ajp);
    }

    HandlerList handlers = new HandlerList();

    // Add a file handler that points to the Opal GWT client directory
    handlers.addHandler(createFileHandler("/webapp"));
    // Add a file handler that points to the Opal BIRT extension update-site
    handlers.addHandler(createFileHandler("/update-site"));

    handlers.addHandler(contextHandler = createServletHandler(ctx, txmgr, securityMgr));
    server.setHandler(handlers);

    this.jettyServer = server;
  }

  @Bean
  public ServletContextHandler getServletContextHandler() {
    return this.contextHandler;
  }

  @Override
  public boolean isRunning() {
    return this.jettyServer.isRunning();
  }

  public void start() {
    try {
      webApplicationContext.refresh();
      log.info("Starting Opal HTTP/s Server on port {}", this.jettyServer.getConnectors()[0].getPort());
      this.jettyServer.start();
    } catch(Exception e) {
      log.error("Error starting jetty", e);
      throw new RuntimeException(e);
    }
  }

  public void stop() {
    try {
      if(webApplicationContext.isActive()) {
        webApplicationContext.close();
      }
    } catch(RuntimeException e) {
      // log and ignore
      log.warn("Exception during web application context shutdown", e);
    }

    try {
      this.jettyServer.stop();
    } catch(Exception e) {
      // log and ignore
      log.warn("Exception during HTTPd server shutdown", e);
    }

  }

  @Override
  public String getName() {
    return null;
  }

  private ServletContextHandler createServletHandler(ApplicationContext ctx, PlatformTransactionManager txmgr,
      SecurityManager securityMgr) {
    ServletContextHandler contextHandler = new ServletContextHandler(
        ServletContextHandler.NO_SESSIONS | ServletContextHandler.NO_SECURITY);
    contextHandler.setContextPath("/");
    contextHandler.addFilter(new FilterHolder(new OpalVersionFilter()), "/*", FilterMapping.DEFAULT);
    contextHandler.addFilter(new FilterHolder(new AuthenticationFilter(securityMgr)), "/ws/*", FilterMapping.DEFAULT);
    // contextHandler.addFilter(new FilterHolder(new X509CertificateAuthenticationFilter()), "/ws/*",
    // FilterMapping.DEFAULT);
    // contextHandler.addFilter(new FilterHolder(new CrossOriginFilter()), "/*", FilterMapping.DEFAULT);
    contextHandler.addFilter(new FilterHolder(new RequestContextFilter()), "/*", FilterMapping.DEFAULT);
    contextHandler.addFilter(new FilterHolder(new TransactionFilter(txmgr)), "/*", FilterMapping.DEFAULT);

    XmlWebApplicationContext webAppCtx = new XmlWebApplicationContext();
    webAppCtx.setParent(ctx);
    webAppCtx.setServletContext(contextHandler.getServletContext());
    webAppCtx.setConfigLocation("classpath:/META-INF/spring/opal-httpd/context.xml");
    contextHandler.getServletContext()
        .setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, webAppCtx);
    this.webApplicationContext = webAppCtx;

    return contextHandler;
  }

  private Handler createFileHandler(String directory) {
    String fileUrl = "file://" + System.getProperty("OPAL_DIST") + directory;
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

  public class OpalVersionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
      try {
        if(opalVersion != null) {
          response.addHeader("X-Opal-Version", opalVersion.toString());
        }
      } catch(RuntimeException e) {
      }

      filterChain.doFilter(request, response);
    }

  }

  public static class TransactionFilter extends OncePerRequestFilter {

    private final PlatformTransactionManager txManager;

    public TransactionFilter(PlatformTransactionManager txManager) {
      this.txManager = txManager;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
        final FilterChain filterChain) throws ServletException, IOException {
      new TransactionTemplate(txManager).execute(new TransactionCallbackWithoutResult() {
        @Override
        protected void doInTransactionWithoutResult(TransactionStatus status) {
          try {
            filterChain.doFilter(request, response);
          } catch(IOException e) {
            throw new RuntimeException(e);
          } catch(ServletException e) {
            throw new RuntimeException(e);
          }
        }
      });

    }

  }

}
