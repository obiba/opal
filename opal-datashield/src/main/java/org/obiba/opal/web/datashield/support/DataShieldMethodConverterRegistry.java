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

import java.util.Set;

import org.obiba.opal.datashield.DataShieldMethod;
import org.obiba.opal.web.model.DataShield.DataShieldMethodDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class DataShieldMethodConverterRegistry {

  private final Set<DataShieldMethodConverter> converters;

  @Autowired
  public DataShieldMethodConverterRegistry(Set<DataShieldMethodConverter> converters) {
    super();
    this.converters = converters;
  }

  public DataShieldMethod parse(DataShieldMethodDto dto) {
    for(DataShieldMethodConverter converter : converters) {
      if(converter.canParse(dto)) {
        return converter.parse(dto);
      }
    }
    throw new NoSuchDataShieldMethodConverterException(dto);
  }

  public DataShieldMethodDto asDto(DataShieldMethod method) {
    for(DataShieldMethodConverter converter : converters) {
      if(converter.accept(method)) {
        return converter.asDto(method);
      }
    }
    throw new NoSuchDataShieldMethodConverterException(method);
  }

}
