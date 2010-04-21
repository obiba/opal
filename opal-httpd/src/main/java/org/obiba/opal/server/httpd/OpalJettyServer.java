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

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.resource.FileResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 *
 */
public class OpalJettyServer implements org.obiba.opal.server.Server {

  private final Server jettyServer;

  private ServletContextHandler contextHandler;

  @Autowired
  public OpalJettyServer(ApplicationContext ctx, PlatformTransactionManager txmgr) {
    Server server = new Server();

    SelectChannelConnector connector0 = new SelectChannelConnector();
    connector0.setPort(8080);
    connector0.setMaxIdleTime(30000);
    connector0.setRequestHeaderSize(8192);

    server.setConnectors(new Connector[] { connector0 });
    HandlerList handlers = new HandlerList();
    handlers.addHandler(createFileHandler());
    handlers.addHandler(createServletHandler(ctx, txmgr));
    server.setHandler(handlers);

    this.jettyServer = server;
  }

  public ServletContextHandler getContext() {
    return this.contextHandler;
  }

  @Override
  public boolean isRunning() {
    return this.jettyServer.isRunning();
  }

  public void start() {
    try {
      this.jettyServer.start();
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void stop() {
    try {
      this.jettyServer.stop();
    } catch(Exception e) {
      // ignore
    }

  }

  private Handler createServletHandler(ApplicationContext ctx, PlatformTransactionManager txmgr) {
    contextHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS | ServletContextHandler.NO_SECURITY);
    contextHandler.setContextPath("/");
    contextHandler.addFilter(new FilterHolder(new DevelopmentModeFilter()), "/*", FilterMapping.DEFAULT);
    contextHandler.addFilter(new FilterHolder(new TransactionFilter(txmgr)), "/*", FilterMapping.DEFAULT);

    // TODO: Should be GenericWebApplicationContext, but cannot due to Jersey bug
    // https://jersey.dev.java.net/issues/show_bug.cgi?id=222
    AnnotationConfigWebApplicationContext webAppCtx = new AnnotationConfigWebApplicationContext();
    webAppCtx.setServletContext(contextHandler.getServletContext());
    webAppCtx.setParent(ctx);
    webAppCtx.refresh();
    contextHandler.getServletContext().setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, webAppCtx);
    return contextHandler;
  }

  /**
   * @return
   */
  private Handler createFileHandler() {
    ResourceHandler resourceHandler = new ResourceHandler();
    try {
      resourceHandler.setBaseResource(new FileResource(new URL("file:///home/plaflamm/opal-home/fs")));
      resourceHandler.setAliases(true);
    } catch(MalformedURLException e) {
      throw new RuntimeException(e);
    } catch(IOException e) {
      throw new RuntimeException(e);
    } catch(URISyntaxException e) {
      throw new RuntimeException(e);
    }
    return resourceHandler;
  }

  public static class TransactionFilter extends OncePerRequestFilter {

    private final PlatformTransactionManager txManager;

    public TransactionFilter(PlatformTransactionManager txManager) {
      this.txManager = txManager;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {
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

  /**
   * Implements HTTP Access-Control to allow XDR requests. Should only be activated during development.
   */
  public static class DevelopmentModeFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
      if(request.getMethod().equalsIgnoreCase("OPTIONS")) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"));
        response.setStatus(200);
        response.flushBuffer();
      } else if(request.getHeader("Origin") != null) {
        response.addHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        filterChain.doFilter(request, response);
      } else {
        filterChain.doFilter(request, response);
      }
    }

  }

}
