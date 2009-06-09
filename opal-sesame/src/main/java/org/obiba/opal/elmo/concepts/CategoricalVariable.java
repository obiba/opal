package org.obiba.opal.elmo.concepts;

import java.util.Set;

import org.openrdf.elmo.annotations.rdf;

@rdf(Opal.NS + "CategoricalVariable")
public interface CategoricalVariable extends DataVariable {

  @rdf(Opal.NS + "hasCategory")
  public Set<Category> getHasCategory();
  public void setHasCategory(Set<Category> categories);

}
