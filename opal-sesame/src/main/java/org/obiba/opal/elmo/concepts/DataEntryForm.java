package org.obiba.opal.elmo.concepts;

import java.util.Set;

import javax.xml.namespace.QName;

import org.obiba.opal.sesame.support.SesameUtil;
import org.openrdf.elmo.annotations.inverseOf;
import org.openrdf.elmo.annotations.rdf;
import org.openrdf.model.URI;

@rdf(Opal.NS + "DataEntryForm")
public interface DataEntryForm extends DataItem {

  public static final QName QNAME = new QName(Opal.NS, "DataEntryForm");

  public static final URI URI = SesameUtil.toUri(QNAME);

  @rdf(Opal.NS + "hasDataItem")
  @inverseOf(Opal.NS + "dataEntryForm")
  public Set<DataItem> getDataItems();

  public void setDataItems();
}
