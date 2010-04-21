package org.obiba.opal.server.httpd;

import org.obiba.opal.core.runtime.Service;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.engine.security.SslContextFactory;
import org.restlet.routing.VirtualHost;

/**
 */
public class OpalHttpServer implements Service {

  private Component component;

  public OpalHttpServer() {
    System.setProperty("org.restlet.engine.loggerFacadeClass", "org.restlet.ext.slf4j.Slf4jLoggerFacade");
    component = new Component();
  }

  public Component getComponent() {
    return component;
  }

  public void addApplication(String root, Application app) {

  }

  public void addApplication(SslContextFactory factory, int port, Application app) throws Exception {
    Context httpsCtx = component.getContext().createChildContext();
    httpsCtx.getAttributes().put("sslContextFactory", factory);
    httpsCtx.getParameters().add("needClientAuthentication", "true");
    httpsCtx.getParameters().add("wantClientAuthentication", "true");

    Server https = new Server(httpsCtx, Protocol.HTTPS, port, (Restlet) null);
    VirtualHost vhost = new VirtualHost(component.getContext().createChildContext());
    vhost.setServerPort(Integer.toString(port));
    vhost.attach(app);
    component.getHosts().add(vhost);
    component.updateHosts();

    component.getServers().add(https);
    https.start();
  }

  @Override
  public boolean isRunning() {
    return component.isStarted();
  }

  public void start() {
    try {
      component.start();
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void stop() {
    try {
      component.stop();
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }

}
