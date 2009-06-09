package org.obiba.opal.elmo.concepts;

import org.openrdf.elmo.annotations.rdf;

@rdf(Opal.NS + "DataVariable")
public interface DataVariable extends DataItem {

  @rdf(Opal.NS + "repeatable")
  public boolean isRepeatable();

  public void setRepeatable(boolean repeatable);

}
