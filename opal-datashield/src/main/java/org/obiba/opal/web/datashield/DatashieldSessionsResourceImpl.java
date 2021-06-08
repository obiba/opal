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

import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.datashield.DataShieldLog;
import org.obiba.opal.datashield.cfg.DatashieldProfile;
import org.obiba.opal.datashield.cfg.DatashieldProfileService;
import org.obiba.opal.r.service.RServerProfile;
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

import javax.ws.rs.ForbiddenException;
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
  private DatashieldProfileService datashieldProfileService;

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

  @Override
  protected RServerProfile createProfile(String profileName) {
    DatashieldProfile profile = datashieldProfileService.getProfile(profileName);
    if (!profile.isEnabled()) {
      String message = datashieldProfileService.hasProfile(profileName) ?
          "DataSHIELD profile is not enabled" : "DataSHIELD profile does not exist";
      throw new IllegalArgumentException(message + ": " + profile.getName());
    }
    // TODO check profile permissions
    return profile;
  }

  protected void onNewRSession(RServerSession rSession) {
    rSession.setExecutionContext(DS_CONTEXT);
    DatashieldProfile profile = (DatashieldProfile) rSession.getProfile();
    if (profile.hasOptions()) {
      rSession.execute(
          new RScriptROperation(DataShieldROptionsScriptBuilder.newBuilder().setROptions(profile.getOptions()).build()));
    }
    rSession.execute(new RScriptROperation(String.format("options('datashield.seed' = %s)", configurationService.getOpalConfiguration().getSeed())));
    DataShieldLog.userLog("created a datashield session {}", rSession.getId());
  }
}
