/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.r.service;

import java.util.SortedSet;

import org.obiba.magma.VariableEntity;

/**
 * An interface to define the contract of persisting the constant set of VariableEntity instances that an OpalRSession
 * uses.
 * <p>
 * An OpalRSession can contain vectors from different ValueTable instances. As such, each vector may have a different
 * set of entities. A constant set of entities is required so that all vectors have the same length. This interface
 * defines the contract for the object holding this constant set.
 */
public interface VariableEntitiesHolder {

  /**
   * Returns true when the holder has a set of entities
   * @return
   */
  public boolean hasEntities();

  /**
   * Returns an immutable view of the set of entities
   * @return
   */
  public SortedSet<VariableEntity> getEntities();

  /**
   * Assign the constant set of entities to work with. This method may only be called once.
   * @param entities
   */
  public void setEntities(SortedSet<VariableEntity> entities);
}
