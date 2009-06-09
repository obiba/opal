package org.obiba.opal.elmo.concepts;

import org.openrdf.elmo.annotations.rdf;

@rdf(Opal.NS + "ContinuousVariable")
public interface ContinuousVariable extends DataVariable {

  @rdf(Opal.NS + "dataValue")
  public Object getValue();

  public void setValue(Object value);

  @rdf(Opal.NS + "unit")
  public String getUnit();

  public void setUnit(String unit);

}
