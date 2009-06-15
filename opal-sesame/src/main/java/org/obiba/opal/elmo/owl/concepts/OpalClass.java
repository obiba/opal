/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.elmo.owl.concepts;

import javax.xml.datatype.XMLGregorianCalendar;

import org.obiba.opal.elmo.concepts.Opal;
import org.openrdf.concepts.owl.Class;
import org.openrdf.elmo.annotations.rdf;

public interface OpalClass extends Class {

  /**
   * The creation date of the Opal class.
   * @return
   */
  @rdf(Opal.NS + "creationDate")
  public XMLGregorianCalendar getCreationDate();

  public void setCreationDate(XMLGregorianCalendar date);

  @rdf(Opal.NS + "creationSource")
  public String getCreationSource();

  public void setCreationSource(String source);

}
