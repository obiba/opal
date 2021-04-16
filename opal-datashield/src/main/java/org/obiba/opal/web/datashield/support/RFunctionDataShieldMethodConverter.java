/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.datashield.support;

import org.obiba.datashield.core.DSMethod;
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
    DataShield.RFunctionDataShieldMethodDto funcDto = dto.getExtension(RFunctionDataShieldMethodDto.method);
    return funcDto.hasRPackage() ? new RFunctionDataShieldMethod(dto.getName(), funcDto.getFunc(),
        funcDto.getRPackage(), funcDto.getVersion()) : new RFunctionDataShieldMethod(dto.getName(), funcDto.getFunc());
  }

  @Override
  public boolean accept(DSMethod method) {
    return method instanceof RFunctionDataShieldMethod;
  }

  @Override
  public DataShieldMethodDto asDto(DSMethod method) {
    RFunctionDataShieldMethod rFunctionMethod = (RFunctionDataShieldMethod) method;
    RFunctionDataShieldMethodDto.Builder builder = DataShield.RFunctionDataShieldMethodDto.newBuilder()
        .setFunc(rFunctionMethod.getFunction());
    if(rFunctionMethod.hasPackage()) {
      builder.setRPackage(rFunctionMethod.getPackage());
      builder.setVersion(rFunctionMethod.getVersion());
    }
    return getDataShieldMethodDtoBuilder(method).setExtension(DataShield.RFunctionDataShieldMethodDto.method, builder.build()).build();
  }

}
