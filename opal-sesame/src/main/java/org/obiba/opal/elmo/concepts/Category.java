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

  @rdf(Opal.NS + "name")
  public String getName();

  public void setName(String name);

  @rdf(Opal.NS + "shortName")
  public String getShortName();

  public void setShortName(String shortName);

  @rdf(Opal.NS + "code")
  public int getCode();

  public void setCode(int code);

}
