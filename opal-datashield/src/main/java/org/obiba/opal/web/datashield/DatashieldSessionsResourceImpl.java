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

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.datashield.DataShieldLog;
import org.obiba.opal.datashield.cfg.DataShieldProfile;
import org.obiba.opal.datashield.cfg.DataShieldProfileService;
import org.obiba.opal.r.service.RServerProfile;
import org.obiba.opal.r.service.RServerSession;
import org.obiba.opal.spi.r.RScriptROperation;
import org.obiba.opal.web.datashield.support.DataShieldROptionsScriptBuilder;
import org.obiba.opal.web.model.OpalR;
import org.obiba.opal.web.r.RSessionsResourceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the list and the creation of the Datashield sessions of the invoking Opal user.
 */
@Component("datashieldSessionsResource")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class DatashieldSessionsResourceImpl extends RSessionsResourceImpl {

  private static final Logger log = LoggerFactory.getLogger(DatashieldSessionsResourceImpl.class);

  static final String DS_CONTEXT = "DataSHIELD";

  @Autowired
  private DataShieldProfileService datashieldProfileService;

  @Autowired
  private OpalConfigurationService configurationService;

  @Value("${org.obiba.opal.security.password.nbHashIterations}")
  private int nbHashIterations;

  @Override
  public List<OpalR.RSessionDto> getRSessions() {
    return super.getRSessions().stream()
        .filter(s -> DS_CONTEXT.equals(s.getContext()))
        .collect(Collectors.toList());
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
    DataShieldProfile profile = datashieldProfileService.getProfile(profileName);
    if (!profile.isEnabled()) {
      String message = datashieldProfileService.hasProfile(profileName) ?
          "DataSHIELD profile is not enabled" : "DataSHIELD profile does not exist";
      throw new IllegalArgumentException(message + ": " + profile.getName());
    }
    // check access
    if (profile.isRestrictedAccess() && !SecurityUtils.getSubject().isPermitted(String.format("rest:/datashield/profile/%s:GET", profile.getName()))) {
      throw new ForbiddenException("DataSHIELD profile access is forbidden: " + profile.getName());
    }
    return profile;
  }

  @Override
  protected boolean createRSessionEnabled() {
    // Datashield service is always available
    return true;
  }

  protected void onNewRSession(RServerSession rSession) {
    rSession.setExecutionContext(DS_CONTEXT);
    DataShieldProfile profile = (DataShieldProfile) rSession.getProfile();
    if (profile.hasOptions()) {
      rSession.execute(
          new RScriptROperation(DataShieldROptionsScriptBuilder.newBuilder().setROptions(profile.getOptions()).build()));
    }
    rSession.execute(new RScriptROperation(String.format("options('datashield.seed' = %s)", getSeed())));
    MDC.put("ds_profile", rSession.getProfile().getName());
    DataShieldLog.userLog(rSession.getId(), DataShieldLog.Action.OPEN, "created a datashield session {}", rSession.getId());
  }

  /**
   * Large seed value, should be smaller than R maximum integer value: .Machine$integer.max.
   *
   * @return
   */
  private long getSeed() {
    String seed = hashPassword(configurationService.getOpalConfiguration().getSecretKey())
        .chars().mapToObj(c -> String.format("%s", c)).collect(Collectors.joining()).substring(0, 9);
    long lseed = Long.parseLong(seed) * 2;
    log.info("datashield.seed = {}", lseed);
    return lseed;
  }

  private String hashPassword(String password) {
    return new Sha512Hash(password, configurationService.getOpalConfiguration().getSecretKey(), nbHashIterations)
        .toString();
  }
}
