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

import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.datashield.DataShieldLog;
import org.obiba.opal.datashield.cfg.DatashieldConfiguration;
import org.obiba.opal.datashield.cfg.DatashieldConfigurationSupplier;
import org.obiba.opal.r.service.RServerSession;
import org.obiba.opal.spi.r.RScriptROperation;
import org.obiba.opal.web.datashield.support.DataShieldROptionsScriptBuilder;
import org.obiba.opal.web.model.OpalR;
import org.obiba.opal.web.r.RSessionsResourceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the list and the creation of the Datashield sessions of the invoking Opal user.
 */
@Component("datashieldSessionsResource")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class DatashieldSessionsResourceImpl extends RSessionsResourceImpl {

  static final String DS_CONTEXT = "DataSHIELD";

  @Autowired
  private DatashieldConfigurationSupplier configurationSupplier;

  @Autowired
  private OpalConfigurationService configurationService;

  @Override
  public List<OpalR.RSessionDto> getRSessionIds() {
    return super.getRSessionIds().stream().filter(s -> DS_CONTEXT.equals(s.getContext())).collect(Collectors.toList());
  }

  @Override
  public Response removeRSessions() {
    opalRSessionManager.getSubjectRSessions().stream()
        .filter(s -> DS_CONTEXT.equals(s.getExecutionContext()))
        .forEach(s -> opalRSessionManager.removeRSession(s.getId()));
    return super.removeRSessions();
  }

  protected void onNewRSession(RServerSession rSession) {
    rSession.setExecutionContext(DS_CONTEXT);
    DatashieldConfiguration config = configurationSupplier.get();
    if (config.hasOptions()) {
      rSession.execute(
          new RScriptROperation(DataShieldROptionsScriptBuilder.newBuilder().setROptions(config.getOptions()).build()));
    }
    rSession.execute(new RScriptROperation(String.format("options('datashield.seed' = %s)", configurationService.getOpalConfiguration().getSeed())));
    DataShieldLog.userLog("created a datashield session {}", rSession.getId());
  }
}
