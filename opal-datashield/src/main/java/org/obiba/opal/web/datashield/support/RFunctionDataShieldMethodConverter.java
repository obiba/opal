/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.datashield.support;

import org.obiba.opal.datashield.DataShieldMethod;
import org.obiba.opal.datashield.RFunctionDataShieldMethod;
import org.obiba.opal.web.model.DataShield;
import org.obiba.opal.web.model.DataShield.DataShieldMethodDto;
import org.obiba.opal.web.model.DataShield.RFunctionDataShieldMethodDto;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class RFunctionDataShieldMethodConverter extends AbstractDataShieldMethodConverter {

  @Override
  public boolean canParse(DataShieldMethodDto dto) {
    return dto.hasExtension(RFunctionDataShieldMethodDto.method);
  }

  @Override
  public DataShieldMethod parse(DataShieldMethodDto dto) {
    return new RFunctionDataShieldMethod(dto.getName(), dto.getExtension(RFunctionDataShieldMethodDto.method).getFunction());
  }

  @Override
  public boolean accept(DataShieldMethod method) {
    return method instanceof RFunctionDataShieldMethod;
  }

  @Override
  public DataShieldMethodDto asDto(DataShieldMethod method) {
    RFunctionDataShieldMethod rFunctionMethod = (RFunctionDataShieldMethod) method;
    DataShield.RFunctionDataShieldMethodDto methodDto = DataShield.RFunctionDataShieldMethodDto.newBuilder().setFunction(rFunctionMethod.getFunction()).build();
    return getDataShieldMethodDtoBuilder(method).setExtension(DataShield.RFunctionDataShieldMethodDto.method, methodDto).build();
  }

}
