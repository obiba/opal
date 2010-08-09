/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma.support;

import org.obiba.magma.DatasourceFactory;
import org.obiba.opal.web.model.Magma.DatasourceFactoryDto;

/**
 * A chain of responsibility for creating a datasource factory from its Dto.
 */
public abstract class DatasourceFactoryDtoParser {
  DatasourceFactoryDtoParser next;

  /**
   * Set the next parser and returns the next for chaining.
   * @param next
   * @return the next
   */
  public DatasourceFactoryDtoParser setNext(DatasourceFactoryDtoParser next) {
    this.next = next;
    return next;
  }

  /**
   * Attempt for creating the factory and if not possible ask to the next parser, if any.
   * @param dto
   * @return null if unable to parse
   */
  public DatasourceFactory parse(DatasourceFactoryDto dto) {
    DatasourceFactory factory = internalParse(dto);
    if(factory == null && next != null) {
      factory = next.parse(dto);
    }
    return factory;
  }

  protected abstract DatasourceFactory internalParse(DatasourceFactoryDto dto);
}
