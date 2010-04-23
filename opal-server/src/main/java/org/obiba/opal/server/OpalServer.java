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

    if(options.isUpgrade()) {
      new UpgradeCommand().execute();
      System.console().printf("Upgrade successful.\n");
    }

    System.console().printf("Starting Opal.\n");
    ctx = new GenericApplicationContext();
    try {
      XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
      xmlReader.loadBeanDefinitions("classpath:/META-INF/spring/opal-server/context.xml");
      ctx.refresh();
    } catch(Exception e) {
      System.console().printf("Failed to start Opal Server. See log file for details.\nError message: %s\n", e.getMessage());
      e.printStackTrace(System.err);
      try {
        ctx.destroy();
      } catch(RuntimeException ignore) {
        // ignore
      }
      return;
    }
  }

  final void startAndWait() throws IOException {
    if(ctx.isActive()) {
      try {
        ctx.getBean(OpalRuntime.class).start();
        System.console().printf("Opal Server successfully started. Type the 'any key' to stop.\n");
        System.in.read();
        ctx.getBean(OpalRuntime.class).stop();
      } finally {
        ctx.destroy();
      }
    }
  }

  public static void main(String[] args) throws IOException {
    try {
      new OpalServer(CliFactory.parseArguments(OpalServerOptions.class, args)).startAndWait();
    } catch(ArgumentValidationException e) {
      System.console().printf("%s\n", e.getMessage());
    }
  }

}
