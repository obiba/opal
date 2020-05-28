/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.datashield;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;

import org.obiba.datashield.core.DSOption;
import org.obiba.opal.datashield.cfg.DatashieldConfiguration;
import org.obiba.opal.datashield.cfg.DatashieldConfigurationSupplier;
import org.obiba.opal.web.model.DataShield;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Scope("request")
@Path("/datashield/options")
public class DataShieldROptionsResourceImpl implements DataShieldROptionsResource {

  private DatashieldConfigurationSupplier configurationSupplier;

  @Autowired
  public void setConfigurationSupplier(DatashieldConfigurationSupplier configurationSupplier) {
    this.configurationSupplier = configurationSupplier;
  }

  @Override
  public List<DataShield.DataShieldROptionDto> getDataShieldROptions() {
    List<DataShield.DataShieldROptionDto> options = new ArrayList<>();
    DatashieldConfiguration config = configurationSupplier.get();

    for(DSOption entry : config.getOptions()) {
      options.add(DataShield.DataShieldROptionDto.newBuilder().setName(entry.getName())//
          .setValue(entry.getValue()).build());
    }

    return options;
  }
}
