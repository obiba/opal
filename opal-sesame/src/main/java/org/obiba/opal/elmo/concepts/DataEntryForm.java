package org.obiba.opal.elmo.concepts;

import java.util.Set;

import javax.xml.namespace.QName;

import org.openrdf.elmo.annotations.inverseOf;
import org.openrdf.elmo.annotations.rdf;

@rdf(Opal.NS + "DataEntryForm")
public interface DataEntryForm extends DataItem {

  public static final QName QNAME = new QName(Opal.NS, "DataEntryForm");

  @rdf(Opal.NS + "hasDataItem")
  @inverseOf(Opal.NS + "dataEntryForm")
  public Set<DataItem> getDataItems();

  public void setDataItems();
}
