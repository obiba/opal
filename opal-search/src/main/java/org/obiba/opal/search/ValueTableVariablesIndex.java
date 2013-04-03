/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.search;

import javax.annotation.Nonnull;

import org.obiba.magma.Attribute;

/**
 * An index of a {@code ValueTable} variables.
 */
public interface ValueTableVariablesIndex extends ValueTableIndex {

  /**
   * Name of the field for the given attribute.
   *
   * @param attribute Attribute
   * @return
   */
  @Nonnull
  String getFieldName(@Nonnull Attribute attribute);

  /**
   * Returns the sort name of the input field. Fields with tokenized value require a special mapping so sorting
   * becomes possible. A field value "toto tata titi" is tokenized to 'toto' 'tata' 'titi' breaking the integrity of
   * the original value and therefore not srotable. These fields require two mappings, one analyzed and not analyzed.
   * See Elastic Search documentation for "multi field type".
   *
   * @param field
   * @return
   */
  @Nonnull
  String getFieldSortName(@Nonnull String field);

}
