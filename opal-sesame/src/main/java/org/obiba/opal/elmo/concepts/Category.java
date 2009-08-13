package org.obiba.opal.elmo.concepts;

import javax.xml.namespace.QName;

import org.openrdf.concepts.rdfs.Resource;
import org.openrdf.elmo.annotations.rdf;

@rdf(Opal.NS + "Category")
public interface Category extends Resource {

  public static final QName QNAME = new QName(Opal.NS, "Category");

  @rdf(Opal.NS + "isCategoryOf")
  public CategoricalItem getCategoricalItem();

  public void setCategoricalItem();

  @rdf(Opal.NS + "code")
  public int getCode();

  public void setCode(int code);

}
