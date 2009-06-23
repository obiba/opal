package org.obiba.opal.elmo.concepts;

import javax.xml.namespace.QName;

import org.openrdf.elmo.annotations.rdf;

@rdf(Opal.NS + "Category")
public interface Category extends DataVariable {

  public static final QName QNAME = new QName(Opal.NS, "Category");

}
