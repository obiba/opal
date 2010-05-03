package org.obiba.opal.server.httpd;

import org.obiba.opal.server.httpd.security.ShiroVerifierAndEnroler;
import org.obiba.opal.server.rest.TransactionFilter;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Protocol;
import org.restlet.ext.jetty.HttpServerHelper;
import org.restlet.ext.jetty.JettyServerHelper;
import org.restlet.security.ChallengeAuthenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

/**
 */
public class OpalHttpServer {

  private Component component;

  private JettyServerHelper jetty;

  @Autowired
  private PlatformTransactionManager txManager;

  public OpalHttpServer() {
    component = new Component();
    Server embeddedServer = new Server(component.getContext().createChildContext(), Protocol.HTTP, 8182, component);
    jetty = new HttpServerHelper(embeddedServer);
  }

  public Component getComponent() {
    return component;
  }

  public void addApplication(String root, Application app) {
    // Executes the request within a transaction
    TransactionFilter tx = new TransactionFilter(txManager);
    tx.setNext(app);

    // Authenticates / authorizes through Shiro
    ChallengeAuthenticator guard = new ChallengeAuthenticator(component.getContext().createChildContext(), ChallengeScheme.HTTP_BASIC, "Opal");
    ShiroVerifierAndEnroler shiro = new ShiroVerifierAndEnroler();
    guard.setVerifier(shiro);
    guard.setEnroler(shiro);
    guard.setNext(tx);

    component.getDefaultHost().attach(root, guard);
  }

  public void start() {
    try {
      jetty.start();
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void stop() {
    try {
      jetty.stop();
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }

}
