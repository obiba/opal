package org.obiba.opal.elmo.concepts;

import java.util.Set;

import javax.xml.namespace.QName;

import org.openrdf.elmo.annotations.inverseOf;
import org.openrdf.elmo.annotations.rdf;

@rdf(Opal.NS + "CategoricalItem")
public interface CategoricalItem extends DataItem {

  public static final QName QNAME = new QName(Opal.NS, "CategoricalItem");

  @rdf(Opal.NS + "hasCategory")
  @inverseOf(Opal.NS + "isCategoryOf")
  public Set<Category> getCategories();

  public void setCategories(Set<Category> categories);

}
