package org.obiba.opal.elmo.concepts;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.obiba.opal.sesame.support.SesameUtil;
import org.openrdf.concepts.rdfs.Resource;
import org.openrdf.elmo.annotations.inverseOf;
import org.openrdf.elmo.annotations.rdf;
import org.openrdf.model.URI;

@rdf(Opal.NS + "Dataset")
public interface Dataset extends Resource {

  public static final QName QNAME = new QName(Opal.NS, "Dataset");

  public static final URI URI = SesameUtil.toUri(QNAME);

  @rdf(Opal.NS + "isForEntity")
  @inverseOf(Opal.NS + "hasDataset")
  public Entity getForEntity();

  public void setForEntity(Entity entity);

  /**
   * The source date of the Dataset.
   * @return
   */
  @rdf(Opal.NS + "sourceDate")
  public XMLGregorianCalendar getSourceDate();

  public void setSourceDate(XMLGregorianCalendar date);

  /**
   * The creation date of the Dataset.
   * @return
   */
  @rdf(Opal.NS + "creationDate")
  public XMLGregorianCalendar getCreationDate();

  public void setCreationDate(XMLGregorianCalendar date);

}
