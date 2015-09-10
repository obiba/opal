package org.obiba.opal.tools;

import com.orientechnologies.orient.core.command.OCommandOutputListener;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.tool.ODatabaseExport;
import com.orientechnologies.orient.server.OServer;

public class LegacyOrientDbExporter {
  private static final String USERNAME = "admin";

  private static final String PASSWORD = "admin";

  public static void main(String[] args) {
    boolean check = false;

    if(args.length < 2 || args.length > 3 || ((check = "--check".equals(args[0])) && args.length > 2)) {

      System.out.println("Usage: java -jar <opal-config-migrator>.jar [--check] <opal_config_dir> [target_file]");
      System.exit(1);
    }

    System.out.println("Legacy OrientDb exporter ... " + (check ? "dry run" : "exporting"));
    OServer server;
    String orientDbHome = !check ? args[0] : args[1];
    String file = !check ? args[1] : null;
    System.setProperty("ORIENTDB_HOME", orientDbHome);
    System.setProperty("ORIENTDB_ROOT_PASSWORD", USERNAME);

    try {
      server = new OServer() //
          .startup(LegacyOrientDbExporter.class.getResourceAsStream("/orientdb-server-config.xml")) //
          .activate();
    } catch(Exception e) {
      throw new RuntimeException("Error starting up embedded OrientDb server", e);
    }

    try(ODatabaseDocumentTx db = ODatabaseDocumentPool.global().acquire("local:" + orientDbHome, USERNAME, PASSWORD)) {
      if(!check) {
        ODatabaseExport export;
        export = new ODatabaseExport(db, file, new OCommandOutputListener() {
          @Override
          public void onMessage(String s) {
            System.out.println(s);
          }
        });

        export.exportDatabase();
        export.close();
      }
    } catch(Exception e) {
      if(!check) throw new RuntimeException("Error exporting legacy OrientDb database", e);

      System.out.println("Invalid OrientDb version detected. Exiting.");
      System.exit(1);
    }

    server.shutdown();
    System.out.println("Legacy OrientDb exporter completed successfully.");
  }
}
