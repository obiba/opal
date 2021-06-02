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
import org.obiba.datashield.core.impl.DefaultDSMethod;
import org.obiba.opal.datashield.DataShieldMethod;
import org.obiba.opal.datashield.PackageDSMethod;
import org.obiba.opal.web.model.DataShield;
import org.obiba.opal.web.model.DataShield.DataShieldMethodDto;
import org.obiba.opal.web.model.DataShield.RFunctionDataShieldMethodDto;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class PackageDSMethodConverter extends AbstractDSMethodConverter {

  @Override
  public boolean canParse(DataShieldMethodDto dto) {
    return dto.hasExtension(RFunctionDataShieldMethodDto.method);
  }

  @Override
  public DataShieldMethod parse(DataShieldMethodDto dto) {
    DataShield.RFunctionDataShieldMethodDto funcDto = dto.getExtension(RFunctionDataShieldMethodDto.method);
    return new PackageDSMethod(dto.getName(), funcDto.getFunc(), funcDto.getRPackage(), funcDto.getVersion());
  }

  @Override
  public boolean accept(DSMethod method) {
    return method.hasPackage();
  }

  @Override
  public DataShieldMethodDto asDto(DSMethod method) {
    DefaultDSMethod rFunctionMethod = (DefaultDSMethod) method;
    RFunctionDataShieldMethodDto.Builder builder = DataShield.RFunctionDataShieldMethodDto.newBuilder()
        .setFunc(rFunctionMethod.getFunction());
    if(rFunctionMethod.hasPackage()) {
      builder.setRPackage(rFunctionMethod.getPackage());
      builder.setVersion(rFunctionMethod.getVersion());
    }
    return getDataShieldMethodDtoBuilder(method).setExtension(DataShield.RFunctionDataShieldMethodDto.method, builder.build()).build();
  }

}
