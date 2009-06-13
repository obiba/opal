/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.elmo.concepts;

import java.util.Set;

import org.openrdf.elmo.annotations.inverseOf;
import org.openrdf.elmo.annotations.rdf;

/**
 *
 */
public interface DataEntryFormClass extends org.openrdf.concepts.owl.Class {

  @rdf(Opal.NS + "containsDataVariable")
  @inverseOf(Opal.NS + "withinDataEntryForm")
  public Set<org.openrdf.concepts.owl.Class> getDataVariables();

  public void setDataVariables(Set<org.openrdf.concepts.owl.Class> variables);

}
