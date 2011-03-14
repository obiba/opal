package org.obiba.opal.server;

import java.io.IOException;

import org.obiba.opal.core.runtime.OpalRuntime;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.CliFactory;
import uk.co.flamingpenguin.jewel.cli.Option;

public class OpalServer {

  public interface OpalServerOptions {

    @Option(description = "Performs an Opal upgrade. Required after Opal installation.")
    public boolean isUpgrade();

  }

  private final GenericApplicationContext ctx;

  OpalServer(OpalServerOptions options) {
    // Tell Carol not to initialize its CMI component. This helps us
    // minimize dependencies brought in by JOTM.
    // See:
    // http://wiki.obiba.org/confluence/display/CAG/Technical+Requirements
    // for details.
    System.setProperty("cmi.disabled", "true");

    // Disable EHCache and Quartz usage tracker
    // http://martijndashorst.com/blog/2011/02/21/ehcache-and-quartz-phone-home-during-startup/
    System.setProperty("net.sf.ehcache.skipUpdateCheck", "true");
    System.setProperty("org.terracotta.quartz.skipUpdateCheck", "true");

    if(options.isUpgrade()) {
      System.out.println("Upgrading Opal.");
      new UpgradeCommand().execute();
      System.out.println("Upgrade successful.");
    }

    System.out.println("Starting Opal.");
    ctx = new GenericApplicationContext();
    try {
      XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
      xmlReader.loadBeanDefinitions("classpath:/META-INF/spring/opal-server/context.xml");
      ctx.refresh();
    } catch(Exception e) {
      System.out.println(String.format("Failed to start Opal Server. See log file for details.\nError message: %s", e.getMessage()));
      e.printStackTrace(System.err);
      try {
        ctx.destroy();
      } catch(RuntimeException ignore) {
        // ignore
      }
      return;
    }
  }

  final void boot() {
    if(ctx.isActive()) {
      ctx.getBean(OpalRuntime.class).start();
      System.out.println("Opal Server successfully started.");
    }
  }

  final void shutdown() {
    System.out.println("Opal Server shuting down.");
    try {
      ctx.getBean(OpalRuntime.class).stop();
    } finally {
      ctx.close();
    }
  }

  private static void checkSystemProperty(String... properties) {
    for(String property : properties) {
      if(System.getProperty(property) == null) {
        throw new IllegalStateException("System property \"" + property + "\" must be defined.");
      }
    }
  }

  public static void main(String[] args) throws ArgumentValidationException, IOException {
    checkSystemProperty("OPAL_HOME", "OPAL_DIST");

    final OpalServer opal = new OpalServer(CliFactory.parseArguments(OpalServerOptions.class, args));
    opal.boot();
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        opal.shutdown();
      }
    });
    System.out.println("Opal is attached to this console. Press ctrl-c to stop.");
    // We can exit the main thread because other non-daemon threads will keep the JVM alive
  }

}
