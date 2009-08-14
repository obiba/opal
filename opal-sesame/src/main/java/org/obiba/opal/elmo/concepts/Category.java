package org.obiba.opal.elmo.concepts;

import javax.xml.namespace.QName;

import org.obiba.opal.sesame.support.SesameUtil;
import org.openrdf.concepts.rdfs.Resource;
import org.openrdf.elmo.annotations.rdf;
import org.openrdf.model.URI;

@rdf(Opal.NS + "Category")
public interface Category extends Resource {

  public static final QName QNAME = new QName(Opal.NS, "Category");

  public static final URI URI = SesameUtil.toUri(QNAME);

  @rdf(Opal.NS + "isCategoryOf")
  public CategoricalItem getCategoricalItem();

  public void setCategoricalItem();

  @rdf(Opal.NS + "code")
  public int getCode();

  public void setCode(int code);

}
