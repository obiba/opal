package org.obiba.opal.elmo.concepts;

import javax.xml.namespace.QName;

import org.openrdf.elmo.annotations.rdf;

@rdf(Opal.NS + "MissingCategory")
public interface MissingCategory extends Category {

  public static final QName QNAME = new QName(Opal.NS, "MissingCategory");

}
