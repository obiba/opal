package org.obiba.opal.elmo.concepts;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.concepts.owl.Class;
import org.openrdf.elmo.annotations.inverseOf;
import org.openrdf.elmo.annotations.rdf;

@rdf(Opal.NS + "Dataset")
public interface Dataset extends Class {

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
