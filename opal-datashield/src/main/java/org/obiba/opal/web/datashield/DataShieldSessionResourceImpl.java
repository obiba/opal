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

import org.obiba.datashield.core.DSMethodType;
import org.obiba.datashield.r.expr.ParseException;
import org.obiba.opal.core.service.DataExportService;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.datashield.DataShieldContext;
import org.obiba.opal.datashield.DataShieldLog;
import org.obiba.opal.datashield.RestrictedRScriptROperation;
import org.obiba.opal.datashield.cfg.DataShieldProfile;
import org.obiba.opal.datashield.cfg.DataShieldProfileService;
import org.obiba.opal.r.service.RCacheHelper;
import org.obiba.opal.r.service.RServerSession;
import org.obiba.opal.spi.r.ROperationWithResult;
import org.obiba.opal.spi.r.RSerialize;
import org.obiba.opal.web.r.AbstractRSessionResource;
import org.obiba.opal.web.r.RSymbolResource;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Component("dataShieldSessionResource")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class DataShieldSessionResourceImpl extends AbstractRSessionResource implements DataShieldSessionResource {

  @Autowired
  private DataShieldProfileService datashieldProfileService;

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private IdentifiersTableService identifiersTableService;

  @Autowired
  private DataExportService dataExportService;

  @Autowired
  private RCacheHelper rCacheHelper;

  @Override
  public Response aggregateBinary(@QueryParam("async") @DefaultValue("false") boolean async, String body) throws ParseException {
    return aggregate(async, body, RSerialize.RAW);
  }

  @Override
  public Response aggregateJSON(@QueryParam("async") @DefaultValue("false") boolean async, String body) throws ParseException {
    return aggregate(async, body, RSerialize.JSON);
  }

  @Override
  public Response lsBinary() {
    beforeLog();
    try {
      Response response = super.lsBinary();
      DataShieldLog.userLog(getRServerSession().getId(), DataShieldLog.Action.LS, "list symbols");
      return response;
    } catch (Throwable e) {
      DataShieldLog.userErrorLog(getRServerSession().getId(), DataShieldLog.Action.LS, "list symbols failed: {}", e.getMessage());
      throw e;
    }
  }

  @Override
  public Response lsJSON() {
    beforeLog();
    try {
      Response response = super.lsJSON();
      DataShieldLog.userLog(getRServerSession().getId(), DataShieldLog.Action.LS, "list symbols");
      return response;
    } catch (Throwable e) {
      DataShieldLog.userErrorLog(getRServerSession().getId(), DataShieldLog.Action.LS, "list symbols failed: {}", e.getMessage());
      throw e;
    }
  }

  @Override
  public Response removeRSession(String saveId) {
    beforeLog();
    try {
      Response response = super.removeRSession(saveId);
      DataShieldLog.userLog(getRServerSession().getId(), DataShieldLog.Action.CLOSE, "closed datashield session {}", getRServerSession().getId());
      return response;
    } catch (Throwable e) {
      DataShieldLog.userErrorLog(getRServerSession().getId(), DataShieldLog.Action.CLOSE, "close datashield session {} failed: {}", getRServerSession().getId(), e.getMessage());
      throw e;
    }
  }

  private Response aggregate(boolean async, String body, RSerialize serialize) throws ParseException {
    RServerSession rSession = getRServerSession();
    DataShieldProfile profile = (DataShieldProfile) rSession.getProfile();
    DataShieldLog.init();
    ROperationWithResult operation = new RestrictedRScriptROperation(body,
        new DataShieldContext(
            profile.getEnvironment(DSMethodType.AGGREGATE),
            rSession.getId(),
            profile.getName(),
            datashieldProfileService.getRParserVersionOrDefault(profile),
            MDC.getCopyOfContextMap()),
        serialize);
    if (async) {
      String id = rSession.executeAsync(operation);
      return Response.ok().entity(id).type(MediaType.TEXT_PLAIN).build();
    } else if (serialize == RSerialize.RAW) {
      rSession.execute(operation);
      return Response.ok().entity(operation.getResult().asBytes()).type(MediaType.APPLICATION_OCTET_STREAM).build();
    } else {
      rSession.execute(operation);
      return Response.ok().entity(operation.getResult().asJSON()).type(MediaType.APPLICATION_JSON).build();
    }
  }

  @Override
  protected RSymbolResource onGetRSymbolResource(String name) {
    DataShieldSymbolResource resource = applicationContext
        .getBean("dataShieldSymbolResource", DataShieldSymbolResource.class);
    resource.setName(name);
    resource.setRServerSession(getRServerSession());
    resource.setIdentifiersTableService(identifiersTableService);
    resource.setDataExportService(dataExportService);
    resource.setRCacheHelper(rCacheHelper);
    resource.setResourceReferenceService(getResourceReferenceService());
    return resource;
  }

  @Override
  protected String getExecutionContext() {
    return DatashieldSessionsResourceImpl.DS_CONTEXT;
  }

  @Override
  public Response saveWorkspace(String saveId) {
    beforeLog();
    try {
      Response response = super.saveWorkspace(saveId);
      if (response.getStatus() == Response.Status.OK.getStatusCode()) {
        DataShieldLog.userLog(getRServerSession().getId(), DataShieldLog.Action.WS_SAVE, "workspace saved: {}", saveId);
      } else {
        DataShieldLog.userErrorLog(getRServerSession().getId(), DataShieldLog.Action.WS_SAVE, "workspace save failed: {}", saveId);
      }
      return response;
    } catch (Throwable e) {
      DataShieldLog.userErrorLog(getRServerSession().getId(), DataShieldLog.Action.WS_SAVE, "workspace save failed: {}, {}", saveId, e.getMessage());
      throw e;
    }
  }

  @Override
  public Response restoreWorkspace(String workspaceId) {
    beforeLog();
    try {
      Response response = super.restoreWorkspace(workspaceId);
      if (response.getStatus() == Response.Status.OK.getStatusCode()) {
        DataShieldLog.userLog(getRServerSession().getId(), DataShieldLog.Action.WS_RESTORE, "workspace restored: {}", workspaceId);
      } else {
        DataShieldLog.userErrorLog(getRServerSession().getId(), DataShieldLog.Action.WS_RESTORE, "workspace restore failed: {}", workspaceId);
      }
      return response;
    } catch (Throwable e) {
      DataShieldLog.userErrorLog(getRServerSession().getId(), DataShieldLog.Action.WS_RESTORE, "workspace restore failed: {}, {}", workspaceId, e.getMessage());
      throw e;
    }
  }

  private void beforeLog() {
    DataShieldLog.init();
    RServerSession rSession = getRServerSession();
    DataShieldProfile profile = (DataShieldProfile) rSession.getProfile();
    MDC.put("ds_profile", profile.getName());
  }
}
