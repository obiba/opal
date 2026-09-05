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
import org.obiba.opal.datashield.DataShieldSessionTraces;
import org.obiba.opal.datashield.DataShieldTracer;
import org.obiba.opal.datashield.RestrictedAssignmentROperation;
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
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

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
    String rid = getRServerSession().getId();
    try {
      Response response = DataShieldTracer.traced(rid, profileName(), DataShieldLog.Action.CLOSE,
          () -> super.removeRSession(saveId));
      DataShieldLog.userLog(rid, DataShieldLog.Action.CLOSE, "closed datashield session {}", rid);
      return response;
    } catch (Throwable e) {
      DataShieldLog.userErrorLog(rid, DataShieldLog.Action.CLOSE, "close datashield session {} failed: {}", rid, e.getMessage());
      throw e;
    } finally {
      // after the CLOSE span, so that it is the last operation of the trace and not of the next one
      DataShieldSessionTraces.end(rid);
    }
  }

  /**
   * The form-encoded body of this inherited method is sent to the R server as scripts: each value goes through the
   * restricted parser, like a symbol assigned by script. The interface declares no checked exception on this method,
   * so a rejected script is turned into the response the {@link ParseExceptionMapper} would have produced.
   */
  @Override
  public Response assign(MultivaluedMap<String, String> symbols) {
    beforeLog();
    if (symbols != null) {
      try {
        for (Map.Entry<String, List<String>> entry : symbols.entrySet()) {
          for (String script : entry.getValue()) {
            getRServerSession().execute(new RestrictedAssignmentROperation(entry.getKey(), script, newDataShieldContext(DSMethodType.ASSIGN)));
          }
        }
      } catch (ParseException e) {
        return new ParseExceptionMapper().toResponse(e);
      }
    }
    return lsBinary();
  }

  private DataShieldContext newDataShieldContext(DSMethodType type) {
    RServerSession rSession = getRServerSession();
    DataShieldProfile profile = (DataShieldProfile) rSession.getProfile();
    return new DataShieldContext(
        profile.getEnvironment(type),
        rSession.getId(),
        profile.getName(),
        datashieldProfileService.getRParserVersionOrDefault(profile),
        MDC.getCopyOfContextMap());
  }

  private Response aggregate(boolean async, String body, RSerialize serialize) throws ParseException {
    RServerSession rSession = getRServerSession();
    DataShieldLog.init();
    ROperationWithResult operation = new RestrictedRScriptROperation(body, newDataShieldContext(DSMethodType.AGGREGATE), serialize);
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
      Response response = DataShieldTracer.traced(getRServerSession().getId(), profileName(), DataShieldLog.Action.WS_SAVE,
          () -> super.saveWorkspace(saveId));
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
      Response response = DataShieldTracer.traced(getRServerSession().getId(), profileName(), DataShieldLog.Action.WS_RESTORE,
          () -> super.restoreWorkspace(workspaceId));
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
    MDC.put("ds_profile", profileName());
  }

  private String profileName() {
    return ((DataShieldProfile) getRServerSession().getProfile()).getName();
  }
}
