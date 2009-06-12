package org.obiba.opal.elmo.concepts;

import org.openrdf.concepts.owl.Class;
import org.openrdf.elmo.annotations.rdf;

@rdf(Opal.NS + "Entity")
public interface Entity extends Class {

  @rdf(Opal.NS + "identifier")
  public String getIdentifier();

  public void setIdentifier(String id);

}
