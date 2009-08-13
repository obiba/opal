package org.obiba.opal.elmo.concepts;

import javax.xml.namespace.QName;

import org.openrdf.concepts.rdfs.Resource;
import org.openrdf.elmo.annotations.inverseOf;
import org.openrdf.elmo.annotations.rdf;

@rdf(Opal.NS + "DataItem")
public interface DataItem extends Resource {

  public static final QName QNAME = new QName(Opal.NS, "DataItem");

  @rdf(Opal.NS + "identifier")
  public String getIdentifier();

  public void setIdentifier();

  @rdf(Opal.NS + "name")
  public String getName();

  public void setName(String name);

  @rdf(Opal.NS + "dataEntryForm")
  @inverseOf(Opal.NS + "hasDataItem")
  public DataEntryForm getDataEntryForm();

  public void setDataEntryForm();

  @rdf(Opal.NS + "dataType")
  public String getDataType();

  public void setDataType(String dataType);

  @rdf(Opal.NS + "repeatable")
  public boolean isRepeatable();

  public void setRepeatable(boolean repeatable);

  @rdf(Opal.NS + "multiple")
  public boolean isMultiple();

  public void setMultiple(boolean multiple);

  @rdf(Opal.NS + "unit")
  public String getUnit();

  public void setUnit(String unit);
}
