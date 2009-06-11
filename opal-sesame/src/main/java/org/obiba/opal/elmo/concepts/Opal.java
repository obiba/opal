package org.obiba.opal.elmo.concepts;

import org.openrdf.concepts.owl.Ontology;
import org.openrdf.elmo.annotations.rdf;

@rdf("http://www.obiba.org/owl/2009/05/opal")
public interface Opal extends Ontology {

  public static final String BASE_URI = "http://www.obiba.org/owl/2009/05/opal";

  public static final String NS = BASE_URI + "#";

}
