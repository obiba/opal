package org.obiba.opal.elmo.concepts;

import javax.xml.namespace.QName;

import org.openrdf.elmo.annotations.rdf;

@rdf(Opal.NS + "ContinuousVariable")
public interface ContinuousVariable extends DataVariable {

  public static final QName QNAME = new QName(Opal.NS, "ContinuousVariable");

  @rdf(Opal.NS + "dataValue")
  public Object getValue();

  public void setValue(Object value);

}
