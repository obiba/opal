/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.datashield.support;

import org.obiba.opal.datashield.DataShieldMethod;
import org.obiba.opal.web.model.DataShield.DataShieldMethodDto;

/**
 *
 */
public class NoSuchDataShieldMethodConverterException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public NoSuchDataShieldMethodConverterException(DataShieldMethod method) {
    super("No such converter for DataShield method with class: " + method.getClass().getSimpleName());
  }

  /**
   * @param dto
   */
  public NoSuchDataShieldMethodConverterException(DataShieldMethodDto dto) {
    super("No such converter for DataShield method Dto with name: " + dto.getName());
  }

}
