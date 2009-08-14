package org.obiba.opal.elmo.concepts;

import javax.xml.namespace.QName;

import org.obiba.opal.sesame.support.SesameUtil;
import org.openrdf.concepts.rdfs.Resource;
import org.openrdf.elmo.annotations.rdf;
import org.openrdf.model.URI;

@rdf(Opal.NS + "Entity")
public interface Entity extends Resource {

  public static final QName QNAME = new QName(Opal.NS, "Entity");

  public static final URI URI = SesameUtil.toUri(QNAME);

  @rdf(Opal.NS + "identifier")
  public String getIdentifier();

  public void setIdentifier(String id);

}
