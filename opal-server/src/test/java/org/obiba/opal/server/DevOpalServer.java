package org.obiba.opal.server;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.obiba.opal.server.httpd.OpalJettyServer;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

//VM args for the launcher: -Xms1G -Xmx2G -XX:MaxPermSize=256M -XX:+UseG1GC
public class DevOpalServer extends OpalJettyServer {

    private static final String[] PROPS = { "OPAL_HOME", "OPAL_DIST", "OPAL_LOG" };
    private static DevOpalServer INSTANCE;

    public static void main(String[] args) throws Exception {

        setOpalSysProperties();
        OpalServer.setProperties();
        OpalServer.configureSLF4JBridgeHandler();

        //making sure we are not log spammed...
        Logger logger = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.INFO);

        new UpgradeCommand().execute(); //this is required to initialize some configuration (mandatory later on)

        INSTANCE = new DevOpalServer();
        INSTANCE.start();
    }

    /**
     * Sets all opal folder system properties relative to ~/.opal instead of /
     * @throws IOException
     */
    private static void setOpalSysProperties() throws IOException {
        Properties props = new Properties();

        File sourcePropsFile = new File("src/main/deb/debian/opal.default");
        InputStream in = new FileInputStream(sourcePropsFile);
        try {
            props.load(in);
        } finally {
            in.close();
        }

        String userHome = System.getProperty("user.home");
        String opalBase = userHome + "/.opal";

        for (String prop: PROPS) {
            System.setProperty(prop, opalBase + props.getProperty(prop)); //'relocate' all folders to  ~/.opal
        }
    }

}
