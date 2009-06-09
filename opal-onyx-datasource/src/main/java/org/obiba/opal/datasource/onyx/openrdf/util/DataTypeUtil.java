package org.obiba.opal.datasource.onyx.openrdf.util;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;

import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;

public class DataTypeUtil {

  static final DatatypeFactory factory;

  static {
    try {
      factory = DatatypeFactory.newInstance();
    } catch(DatatypeConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  public static QName getQName(DataType type) {
    switch(type) {
    case BOOLEAN:
      return QName.valueOf("xsd:boolean");
    case DATA:
      return QName.valueOf("xsd:base64binary");
    case DATE:
      return QName.valueOf("xsd:dateTime");
    case DECIMAL:
      return QName.valueOf("xsd:decimal");
    case INTEGER:
      return QName.valueOf("xsd:integer");
    case TEXT:
      return QName.valueOf("xsd:string");
    }
    return null;
  }

  public static Object getValue(Data data) {
    switch(data.getType()) {
    case DATE:
      GregorianCalendar c = new GregorianCalendar();
      c.setTime((Date) data.getValue());
      return factory.newXMLGregorianCalendar(c);
    default:
      return data.getValue();
    }
  }

}
