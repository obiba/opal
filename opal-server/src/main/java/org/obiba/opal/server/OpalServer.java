/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.server;

import javax.validation.constraints.NotNull;

import com.google.common.base.Strings;
import org.obiba.opal.core.service.ApplicationContextProvider;
import org.obiba.opal.core.service.event.OpalStartedEvent;
import org.obiba.opal.server.httpd.OpalJettyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class OpalServer {

  private static final Logger log = LoggerFactory.getLogger(OpalServer.class);

  private OpalJettyServer jettyServer;

  private OpalServer(boolean upgrade) {
    setProxy();
    setProperties();
    configureSLF4JBridgeHandler();
    if (upgrade) {
      upgrade();
    }
    else {
      asciiArt();
      log.info("Starting Opal server!");
      start();
    }
  }

  // http://patorjk.com/software/taag/#p=display&f=Big&t=%3E%20%3E%20%3E%20OPAL
  private void asciiArt() {
    System.out.println(" __    __    __      ____  _____        _      \n" +
        " \\ \\   \\ \\   \\ \\    / __ \\|  __ \\ /\\   | |     \n" +
        "  \\ \\   \\ \\   \\ \\  | |  | | |__) /  \\  | |     \n" +
        "   > >   > >   > > | |  | |  ___/ /\\ \\ | |     \n" +
        "  / /   / /   / /  | |__| | |  / ____ \\| |____ \n" +
        " /_/   /_/   /_/    \\____/|_| /_/    \\_\\______|\n" +
        "                                               \n" +
        "                                               ");
  }

  private void setProxy() {
    String proxyUser = System.getProperty("http.proxyUser", System.getProperty("https.proxyUser"));
    if (!Strings.isNullOrEmpty(proxyUser)) {
      logAndSystemOut("Setting up proxy with user/password...");
      // Java ignores http.proxyUser. Here come's the workaround.
      Authenticator.setDefault(new Authenticator() {
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
          String prot = getRequestingProtocol().toLowerCase();
          log.info("requestorType={} requestingProtocol={} requestingHost={} requestingPort={}", getRequestorType(), prot, getRequestingHost(), getRequestingPort());
          String requestorType = System.getProperty("requestorType", "proxy"); // can be 'proxy', 'server' or 'any'
          if (requestorType.equalsIgnoreCase("any") || requestorType.equalsIgnoreCase(getRequestorType().toString())) {
            String host = System.getProperty(prot + ".proxyHost", "");
            String port = System.getProperty(prot + ".proxyPort", "https".equals(prot) ? "443" : "80");
            String user = System.getProperty(prot + ".proxyUser", "");
            String password = System.getProperty(prot + ".proxyPassword", "");
            log.info("proxyHost={} proxyPort={} proxyUser={} proxyPassword={}", host, port, user, Strings.isNullOrEmpty(password) ? "": "********");
            if (host.equalsIgnoreCase(getRequestingHost())) {
              if (Integer.parseInt(port) == getRequestingPort()) {
                return new PasswordAuthentication(user, password.toCharArray());
              }
            }
          }
          return null;
        }
      });
    }
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
    System.setProperty("com.atomikos.icatch.registered", "true");
  }

  private void upgrade() {
    logAndSystemOut("Upgrading Opal...");
    new UpgradeCommand().execute();
    logAndSystemOut("Upgrade successful.");
  }

  private void start() {
    logAndSystemOut("Starting Opal...");
    jettyServer = new OpalJettyServer();
    try {
      jettyServer.start();
      logAndSystemOut("Opal Server successfully started.");
      ApplicationContextProvider.getApplicationContext().publishEvent(new OpalStartedEvent());
    } catch(Exception e) {
      log.error("Exception while starting Opal", e);
      System.out.println(
          String.format("Failed to start Opal Server. See log file for details.\nError message: %s", e.getMessage()));
      e.printStackTrace(System.err);
      try {
        jettyServer.stop();
      } catch(Exception ignore) {
        // ignore
      }
    }
  }

  private void shutdown() {
    logAndSystemOut("Opal Server shutting down...");
    try {
      jettyServer.stop();
    } catch(Exception e) {
      log.warn("Exception during HTTPd server shutdown", e);
    }
  }

  private void logAndSystemOut(String msg) {
    log.info(msg);
    System.out.println(msg);
  }

  private static void checkSystemProperty(@NotNull String... properties) {
    for(String property : properties) {
      if(System.getProperty(property) == null) {
        throw new IllegalStateException("System property \"" + property + "\" must be defined.");
      }
    }
  }

  public static void main(String... args) throws Exception {
    try {
      checkSystemProperty("OPAL_HOME", "OPAL_DIST");

      if (args.length>0) {
        if ("--upgrade".equals(args[0])) {

          new OpalServer(true);
          System.exit(0);
        } else {
          System.out.println("Unknown arguments, exiting");
          System.exit(1);
        }
      } else {
        final OpalServer opal = new OpalServer(false);
        Runtime.getRuntime().addShutdownHook(new Thread() {
          @Override
          public void run() {
            opal.shutdown();
          }
        });
        System.out.println("Opal is attached to this console. Press ctrl-c to stop.");
        // We can exit the main thread because other non-daemon threads will keep the JVM alive
      }
    } catch(Exception e) {
      log.error("Exception", e);
      throw e;
    }
  }

}
