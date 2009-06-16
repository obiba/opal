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

import java.util.Set;

import org.obiba.opal.elmo.concepts.Opal;
import org.openrdf.elmo.annotations.rdf;

/**
 * 
 */
public interface DataItemClass extends OpalClass {

  @rdf(Opal.NS + "parent")
  public DataItemClass getParent();

  public void setParent(DataItemClass parent);

  @rdf(Opal.NS + "child")
  public Set<DataItemClass> getChildren();

  public void setChildren(Set<DataItemClass> children);

  @rdf(Opal.NS + "multiple")
  public boolean isMultiple();

  public void setMultiple(boolean multiple);

  /**
   * The condition information.
   * @return
   */
  @rdf(Opal.NS + "condition")
  public String getCondition();

  public void setCondition(String condition);

  /**
   * The occurrence information.
   * @return
   */
  @rdf(Opal.NS + "occurrenceCount")
  public String getOccurrenceCount();

  public void setOccurrenceCount(String occurrence);

  /**
   * The source of information.
   * @return
   */
  @rdf(Opal.NS + "source")
  public String getSource();

  public void setSource(String source);

  /**
   * The validation information.
   * @return
   */
  @rdf(Opal.NS + "validation")
  public String getValidation();

  public void setValidation(String validation);

  /**
   * The name of the Onyx variable.
   * @return
   */
  @rdf(Opal.NS + "name")
  public String getName();

  public void setName(String name);

  /**
   * The path of the Onyx variable.
   * @return
   */
  @rdf(Opal.NS + "path")
  public String getPath();

  public void setPath(String path);

  /**
   * The name of the Opal class.
   * @return
   */
  @rdf(Opal.NS + "className")
  public String getClassName();

  public void setClassName(String name);

}
