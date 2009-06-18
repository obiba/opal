package org.obiba.opal.elmo.concepts;

import org.openrdf.concepts.rdfs.Resource;
import org.openrdf.elmo.annotations.rdf;

@rdf(Opal.NS + "Entity")
public interface Entity extends Resource {

  @rdf(Opal.NS + "identifier")
  public String getIdentifier();

  public void setIdentifier(String id);

}
