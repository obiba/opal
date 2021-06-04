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
import org.obiba.opal.datashield.cfg.DatashieldConfig;
import org.obiba.opal.datashield.cfg.DatashieldConfigService;
import org.obiba.opal.r.service.RServerManagerService;
import org.obiba.opal.web.model.DataShield;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.Path;
import java.util.ArrayList;
import java.util.List;

@Component
@Transactional
@Scope("request")
@Path("/datashield/options")
public class DataShieldROptionsResourceImpl implements DataShieldROptionsResource {

  @Autowired
  private DatashieldConfigService datashieldConfigService;

  @Override
  public List<DataShield.DataShieldROptionDto> getDataShieldROptions(String profile) {
    List<DataShield.DataShieldROptionDto> options = new ArrayList<>();
    DatashieldConfig config = datashieldConfigService.getConfiguration(profile);

    for (DSOption entry : config.getOptions()) {
      options.add(DataShield.DataShieldROptionDto.newBuilder().setName(entry.getName())//
          .setValue(entry.getValue()).build());
    }

    return options;
  }
}
