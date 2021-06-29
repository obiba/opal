/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.datashield;

import com.google.common.base.Strings;
import org.obiba.datashield.core.DSMethod;
import org.obiba.datashield.core.impl.DefaultDSMethod;
import org.obiba.opal.datashield.cfg.DataShieldProfile;
import org.obiba.opal.web.model.DataShield;

/**
 * Utility class for building Datashield related Dtos.
 */
public class Dtos {

  private Dtos() {
  }

  public static DataShield.DataShieldProfileDto asDto(DataShieldProfile profile) {
    DataShield.DataShieldProfileDto.Builder builder = DataShield.DataShieldProfileDto.newBuilder()
        .setName(profile.getName())
        .setCluster(profile.getCluster())
        .setEnabled(profile.isEnabled())
        .setRestrictedAccess(profile.isRestrictedAccess());

    if (profile.hasRParserVersion())
        builder.setRParser(profile.getRParserVersion());

    return builder.build();
  }

  public static DataShieldProfile fromDto(DataShield.DataShieldProfileDto dto) {
    DataShieldProfile profile = new DataShieldProfile(dto.getName());
    profile.setCluster(dto.getCluster());
    profile.setEnabled(dto.getEnabled());
    profile.setRestrictedAccess(dto.getRestrictedAccess());
    profile.setRParserVersion(dto.hasRParser() ? dto.getRParser() : null);
    return profile;
  }


  public static DefaultDSMethod fromDto(DataShield.DataShieldMethodDto dto) {
    if (dto.hasExtension(DataShield.RFunctionDataShieldMethodDto.method)) {
      DataShield.RFunctionDataShieldMethodDto funcDto = dto.getExtension(DataShield.RFunctionDataShieldMethodDto.method);
      String pkg = funcDto.getRPackage();
      if (Strings.isNullOrEmpty(funcDto.getRPackage())) {
        String[] funcTokens = funcDto.getFunc().split("::");
        if (funcTokens.length > 1)
          pkg = funcTokens[0];
      }
      return new DefaultDSMethod(dto.getName(), funcDto.getFunc(), pkg, funcDto.getVersion());
    }

    return new DefaultDSMethod(dto.getName(), dto.getExtension(DataShield.RScriptDataShieldMethodDto.method).getScript());
  }

  public static DataShield.DataShieldMethodDto asDto(DSMethod method) {
    DataShield.DataShieldMethodDto.Builder mBuilder = DataShield.DataShieldMethodDto.newBuilder().setName(method.getName());

    if (method.hasPackage()) {
      DefaultDSMethod rFunctionMethod = (DefaultDSMethod) method;
      DataShield.RFunctionDataShieldMethodDto.Builder builder = DataShield.RFunctionDataShieldMethodDto.newBuilder()
          .setFunc(rFunctionMethod.getFunction());
      if (rFunctionMethod.hasPackage()) {
        builder.setRPackage(rFunctionMethod.getPackage());
        builder.setVersion(rFunctionMethod.getVersion());
      }
      mBuilder.setExtension(DataShield.RFunctionDataShieldMethodDto.method, builder.build());
    } else {
      DefaultDSMethod rScriptMethod = (DefaultDSMethod) method;
      DataShield.RScriptDataShieldMethodDto methodDto = DataShield.RScriptDataShieldMethodDto.newBuilder()
          .setScript(rScriptMethod.getFunction()).build();
      mBuilder.setExtension(DataShield.RScriptDataShieldMethodDto.method, methodDto);
    }

    return mBuilder.build();
  }
}
