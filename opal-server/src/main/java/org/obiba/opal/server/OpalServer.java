package org.obiba.opal.server;

import org.obiba.opal.core.service.impl.LocalOrientDbServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;

import uk.co.flamingpenguin.jewel.cli.CliFactory;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class OpalServer {

  private static final Logger log = LoggerFactory.getLogger(OpalServer.class);

  public interface OpalServerOptions {

  }

  private GenericApplicationContext ctx;

  OpalServer(OpalServerOptions options) {
    setProperties();

    configureSLF4JBridgeHandler();

    //TODO remove this static access when restarting embedded server will work
    LocalOrientDbServerFactory
        .start(LocalOrientDbServerFactory.URL.replace("${OPAL_HOME}", System.getProperty("OPAL_HOME")));

    upgrade();
    start();
  }

  /**
   * Bridge/route all java.util.logging log records to the SLF4J API.
   */
  private void configureSLF4JBridgeHandler() {
    //  remove existing handlers attached to java.util.logging root logger
    SLF4JBridgeHandler.removeHandlersForRootLogger();

    // add SLF4JBridgeHandler to java.util.logging's root logger
    SLF4JBridgeHandler.install();
  }

  private void setProperties() {
    // Disable EHCache and Quartz usage tracker
    // http://martijndashorst.com/blog/2011/02/21/ehcache-and-quartz-phone-home-during-startup
    System.setProperty("net.sf.ehcache.skipUpdateCheck", "true");
    System.setProperty("org.terracotta.quartz.skipUpdateCheck", "true");
  }

  private void upgrade() {
    System.out.println("Upgrading Opal.");
    new UpgradeCommand().execute();
    System.out.println("Upgrade successful.");
  }

  private void start() {
    System.out.println("Starting Opal.");
    ctx = new GenericApplicationContext();
    try {
      XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
      xmlReader.loadBeanDefinitions("classpath:/META-INF/spring/opal-server/context.xml");
      ctx.refresh();
    } catch(Exception e) {
      System.out.println(
          String.format("Failed to start Opal Server. See log file for details.\nError message: %s", e.getMessage()));
      e.printStackTrace(System.err);
      try {
        ctx.destroy();
      } catch(RuntimeException ignore) {
        // ignore
      }
    }
  }

  final void boot() {
    if(ctx.isActive()) {
      System.out.println("Opal Server successfully started.");

    }
  }

  final void shutdown() {
    System.out.println("Opal Server shutting down...");
    ctx.close();
    //TODO remove this static access when restarting embedded server will work
    LocalOrientDbServerFactory.stop();
  }

  private static void checkSystemProperty(String... properties) {
    for(String property : properties) {
      if(System.getProperty(property) == null) {
        throw new IllegalStateException("System property \"" + property + "\" must be defined.");
      }
    }
  }

  public static void main(String... args) throws Exception {
    try {
      checkSystemProperty("OPAL_HOME", "OPAL_DIST");

      final OpalServer opal = new OpalServer(CliFactory.parseArguments(OpalServerOptions.class, args));
      opal.boot();
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          opal.shutdown();
        }
      });
      System.out.println("Opal is attached to this console. Press ctrl-c to stop.");
      // We can exit the main thread because other non-daemon threads will keep the JVM alive
    } catch(Exception e) {
      log.error("Exception", e);
      throw e;
    }
  }

}
