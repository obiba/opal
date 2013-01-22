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
 * A datasource factory from its Dto.
 */
public interface DatasourceFactoryDtoParser {

  /**
   * Parses the provided {@code dto} instance and builds a corresponding {@code DatasourceFactory} instance.
   *
   * @param dto
   * @return the factory
   */
  DatasourceFactory parse(DatasourceFactoryDto dto);

  /**
   * Returns true when this instance is capable of parsing {@code dto} and convert it into a {@code DatasourceFactory}
   *
   * @param dto
   * @return
   */
  boolean canParse(DatasourceFactoryDto dto);
}
