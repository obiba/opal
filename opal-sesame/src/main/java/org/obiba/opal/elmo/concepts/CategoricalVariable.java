package org.obiba.opal.elmo.concepts;

import java.util.Set;

import javax.xml.namespace.QName;

import org.openrdf.elmo.annotations.rdf;

@rdf(Opal.NS + "CategoricalVariable")
public interface CategoricalVariable extends DataVariable {

  public static final QName QNAME = new QName(Opal.NS, "CategoricalVariable");

  @rdf(Opal.NS + "hasCategory")
  public Set<Category> getHasCategory();

  public void setHasCategory(Set<Category> categories);

}
