package org.obiba.opal.elmo.concepts;

import org.openrdf.concepts.owl.Class;
import org.openrdf.elmo.annotations.inverseOf;
import org.openrdf.elmo.annotations.rdf;

@rdf(Opal.NS + "Dataset")
public interface Dataset extends Class {

  @rdf(Opal.NS + "isForEntity")
  @inverseOf(Opal.NS + "hasDataset")
  public Entity getForEntity();

  public void setForEntity(Entity entity);

}
