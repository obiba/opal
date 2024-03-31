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

import org.obiba.datashield.core.DSOption;
import org.obiba.opal.datashield.cfg.DataShieldProfile;
import org.obiba.opal.datashield.cfg.DataShieldProfileService;
import org.obiba.opal.web.model.DataShield;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@Transactional
@Scope("request")
@Path("/datashield/options")
public class DataShieldROptionsResourceImpl implements DataShieldROptionsResource {

  @Autowired
  private DataShieldProfileService datashieldProfileService;

  @Override
  public List<DataShield.DataShieldROptionDto> getDataShieldROptions(String profile) {
    List<DataShield.DataShieldROptionDto> options = new ArrayList<>();
    DataShieldProfile config = getDataShieldProfile(profile);

    for (DSOption entry : config.getOptions()) {
      options.add(DataShield.DataShieldROptionDto.newBuilder().setName(entry.getName())//
          .setValue(entry.getValue()).build());
    }

    options.sort(Comparator.comparing(DataShield.DataShieldROptionDto::getName));

    return options;
  }

  @Override
  public Response deleteDataShieldROptions(List<String> names, String profile) {
    DataShieldProfile config = getDataShieldProfile(profile);
    List<DSOption> options = StreamSupport.stream(config.getOptions().spliterator(), false)
        .filter(o -> (names == null || names.isEmpty() || names.contains(o.getName())))
        .collect(Collectors.toList());
    if (!options.isEmpty()) {
      for (DSOption option : options)
        config.removeOption(option.getName());
      datashieldProfileService.saveProfile(config);
    }
    return Response.ok().build();
  }

  private DataShieldProfile getDataShieldProfile(String profileName) {
    return datashieldProfileService.getProfile(profileName);
  }
}
