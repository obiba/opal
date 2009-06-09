package org.obiba.opal.elmo.concepts;

import java.util.Set;

import org.openrdf.concepts.owl.Class;
import org.openrdf.elmo.annotations.inverseOf;
import org.openrdf.elmo.annotations.rdf;

@rdf(Opal.NS + "DataItem")
public interface DataItem extends Class {

  @rdf(Opal.NS + "withinDataset")
  @inverseOf(Opal.NS + "hasData")
  public Set<Dataset> getWithinDataset();
  public void setWithinDataset(Set<Dataset> datasets);

}
