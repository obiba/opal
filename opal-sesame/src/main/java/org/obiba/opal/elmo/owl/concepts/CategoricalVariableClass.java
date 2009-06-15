package org.obiba.opal.elmo.owl.concepts;

import java.util.Set;

import org.obiba.opal.elmo.concepts.Opal;
import org.openrdf.elmo.annotations.rdf;

public interface CategoricalVariableClass extends DataItemClass {

  @rdf(Opal.NS + "category")
  public Set<CategoryClass> getCategories();

  public void setCategories(Set<CategoryClass> children);

}
