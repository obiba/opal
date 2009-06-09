package org.obiba.opal.elmo;

import java.io.File;
import java.io.IOException;

import org.openrdf.OpenRDFException;
import org.openrdf.elmo.ElmoModule;
import org.openrdf.elmo.sesame.SesameManager;
import org.openrdf.elmo.sesame.SesameManagerFactory;
import org.openrdf.rio.RDFFormat;

public class OpalManagerFactory {

  private OpalManagerFactory() {
  }

  public static SesameManager createManager() throws OpenRDFException, IOException {
    ElmoModule module = OpalElmoModuleFactory.createInstance();
    SesameManagerFactory factory = new SesameManagerFactory(module);
    SesameManager manager = factory.createElmoManager();
    manager.getConnection().add(new File("src/main/owl/opal.owl"), "http://www.obiba.org/owl/2009/05/opal", RDFFormat.RDFXML);
    return manager;
  }
}
