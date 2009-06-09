package org.obiba.opal.elmo;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.openrdf.OpenRDFException;
import org.openrdf.concepts.owl.OwlProperty;
import org.openrdf.elmo.annotations.rdf;
import org.openrdf.elmo.sesame.SesameManager;

public class OpalOntologyManager {

  private SesameManager manager;

  public OpalOntologyManager() throws OpenRDFException, IOException {
    this.manager = OpalManagerFactory.createManager();
  }

  public <T extends org.openrdf.concepts.owl.Class> org.openrdf.concepts.owl.Class getOpalClass(Class<T> type) {
    return getOpalNode(type, org.openrdf.concepts.owl.Class.class);

  }

  public <T extends OwlProperty> T getOpalProperty(Class<T> opalType) {
    return (T) getOpalNode(opalType, OwlProperty.class);

  }

  public <T> T getOpalNode(Class<? extends T> type, Class<T> actualType) {
    rdf r = type.getAnnotation(rdf.class);
    if(r == null) {
      throw new IllegalArgumentException("Type not annotated with @rdf: " + type);
    }

    String[] types = r.value();

    T e = manager.find(actualType, QName.valueOf(types[0]));
    if(e == null) {
      throw new IllegalArgumentException("No entity with uri " + types[0] + " of type " + actualType);
    }
    return e;
  }
}
