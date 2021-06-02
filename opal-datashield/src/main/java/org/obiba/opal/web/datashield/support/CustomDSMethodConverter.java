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
import org.obiba.opal.datashield.CustomDSMethod;
import org.obiba.opal.datashield.DataShieldMethod;
import org.obiba.opal.web.model.DataShield;
import org.obiba.opal.web.model.DataShield.DataShieldMethodDto;
import org.obiba.opal.web.model.DataShield.RScriptDataShieldMethodDto;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class CustomDSMethodConverter extends AbstractDSMethodConverter {

  @Override
  public boolean canParse(DataShieldMethodDto dto) {
    return dto.hasExtension(RScriptDataShieldMethodDto.method);
  }

  @Override
  public DataShieldMethod parse(DataShieldMethodDto dto) {
    return new CustomDSMethod(dto.getName(), dto.getExtension(RScriptDataShieldMethodDto.method).getScript());
  }

  @Override
  public boolean accept(DSMethod method) {
    return !method.hasPackage();
  }

  @Override
  public DataShieldMethodDto asDto(DSMethod method) {
    DefaultDSMethod rScriptMethod = (DefaultDSMethod) method;
    DataShield.RScriptDataShieldMethodDto methodDto = DataShield.RScriptDataShieldMethodDto.newBuilder()
        .setScript(rScriptMethod.getFunction()).build();
    return getDataShieldMethodDtoBuilder(method).setExtension(DataShield.RScriptDataShieldMethodDto.method, methodDto)
        .build();
  }

}
