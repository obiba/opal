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

    if(args.length < 2 || args.length > 3 || ((check = "--check".equals(args[0])) && args.length > 2) ) {

      System.out.println("Usage: java -jar <opal-config-migrator>.jar [--check] <opal_config_dir> [target_file]");
      System.exit(1);
    }

    System.out.println("Legacy OrientDb exporter ... " + (check ? "dry run" : "exporting"));
    OServer server;
    String orientDbHome = !check ? args[0] : args[1];
    String file = !check ? args[1] : null;

    try {
      System.setProperty("ORIENTDB_HOME", orientDbHome);
      System.setProperty("ORIENTDB_ROOT_PASSWORD", USERNAME);

      server = new OServer() //
          .startup(LegacyOrientDbExporter.class.getResourceAsStream("/orientdb-server-config.xml")) //
          .activate();

      try(ODatabaseDocumentTx db = ODatabaseDocumentPool.global()
          .acquire("local:" + orientDbHome, USERNAME, PASSWORD)) {

        if(!check) {
          ODatabaseExport export = new ODatabaseExport(db, file, new OCommandOutputListener() {
            @Override
            public void onMessage(String s) {
              System.out.println(s);
            }
          });

          export.exportDatabase();
          export.close();
        }
      }

      server.shutdown();

      if(!check) System.out.println("Legacy OrientDb exporter completed successfully.");
      else System.out.println("Legacy OrientDb exporter check completed successfully.");
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }
}
