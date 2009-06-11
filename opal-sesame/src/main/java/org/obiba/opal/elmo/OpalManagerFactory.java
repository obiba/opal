package org.obiba.opal.elmo;

import java.io.IOException;
import java.io.InputStream;

import org.obiba.core.util.StreamUtil;
import org.obiba.opal.elmo.concepts.Opal;
import org.openrdf.OpenRDFException;
import org.openrdf.elmo.ElmoModule;
import org.openrdf.elmo.sesame.SesameManager;
import org.openrdf.elmo.sesame.SesameManagerFactory;
import org.openrdf.rio.RDFFormat;

public class OpalManagerFactory {

  private OpalManagerFactory() {
  }

  public static SesameManager createManager() throws OpenRDFException, IOException {
    ElmoModule module = new ElmoModule();
    SesameManagerFactory factory = new SesameManagerFactory(module);
    SesameManager manager = factory.createElmoManager();
    InputStream opalIs = OpalManagerFactory.class.getResourceAsStream("/META-INF/opal.owl");
    try {
      manager.getConnection().add(opalIs, Opal.BASE_URI, RDFFormat.RDFXML);
    } finally {
      StreamUtil.silentSafeClose(opalIs);
    }
    return manager;
  }
}
