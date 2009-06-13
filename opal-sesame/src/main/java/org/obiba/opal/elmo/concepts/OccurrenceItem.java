package org.obiba.opal.elmo.concepts;

import java.util.Set;

import org.openrdf.elmo.annotations.inverseOf;
import org.openrdf.elmo.annotations.rdf;

@rdf(Opal.NS + "OccurrenceItem")
public interface OccurrenceItem extends DataItem {

  @rdf(Opal.NS + "identifier")
  public String getIdentifier();

  public void setIdentifier(String id);

  @rdf(Opal.NS + "ordinal")
  public Integer getOrdinal();

  public void setOrdinal(Integer i);

  @rdf(Opal.NS + "hasOccurrence")
  @inverseOf(Opal.NS + "withinOccurrence")
  public Set<DataVariable> getHasOccurrenceData();

  public void setHasOccurrenceData(Set<DataVariable> datasets);

}
