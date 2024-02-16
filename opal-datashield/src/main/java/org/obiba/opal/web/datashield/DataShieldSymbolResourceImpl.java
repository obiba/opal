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
import org.obiba.opal.core.service.NoSuchResourceReferenceException;
import org.obiba.opal.core.service.ResourceAssignException;
import org.obiba.opal.datashield.DataShieldContext;
import org.obiba.opal.datashield.DataShieldLog;
import org.obiba.opal.datashield.RestrictedAssignmentROperation;
import org.obiba.opal.datashield.cfg.DataShieldProfile;
import org.obiba.opal.datashield.cfg.DataShieldProfileService;
import org.obiba.opal.datashield.cfg.RestrictedROperation;
import org.obiba.opal.r.magma.MagmaAssignROperation;
import org.obiba.opal.r.service.RServerSession;
import org.obiba.opal.spi.r.ROperation;
import org.obiba.opal.spi.r.RServerConnection;
import org.obiba.opal.web.r.AbstractRSymbolResourceImpl;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;

@Component("dataShieldSymbolResource")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class DataShieldSymbolResourceImpl extends AbstractRSymbolResourceImpl implements DataShieldSymbolResource {

  @Autowired
  private DataShieldProfileService datashieldProfileService;

  @Value("#{new Boolean('${datashield.useTibble}')}")
  private boolean useTibble;

  @Override
  public Response putTable(UriInfo uri, String path, String variableFilter, Boolean withMissings, String idName, String identifiersMapping, String rClass, boolean async) {
    logInit();
    MDC.put("ds_table", path);
    return super.putTable(uri, path, variableFilter, withMissings, idName, identifiersMapping, rClass, async);
  }

  @Override
  public Response putResource(UriInfo uri, String path, boolean async) {
    logInit();
    MDC.put("ds_resource", path);
    try {
      return super.putResource(uri, path, async);
    } catch (NoSuchResourceReferenceException | ResourceAssignException e) {
      DataShieldLog.userErrorLog(newDataShieldContext(), DataShieldLog.Action.ASSIGN, "assignment failure from '{} <- resource[{}]': {}", getName(), path, e.getMessage());
      throw e;
    }
  }

  @Override
  public Response putRScript(UriInfo uri, String script, boolean async) throws Exception {
    logInit();
    return putRestrictedRScript(uri, script, async);
  }

  @Override
  public Response putString(UriInfo uri, String content, boolean async) {
    logInit();
    MDC.put("ds_expr", String.format("\"%s\"", content));
    return super.putString(uri, content, async);
  }

  @Override
  public Response rm() {
    logInit();
    Response response = super.rm();
    DataShieldLog.userLog(newDataShieldContext(), DataShieldLog.Action.RM, "deleted symbol '{}'", getName());
    return response;
  }

  @Override
  public Response getSymbolBinary() {
    return Response.status(Status.FORBIDDEN).build();
  }

  @Override
  public Response getSymbolJSON() {
    return Response.status(Status.FORBIDDEN).build();
  }

  protected Response putRestrictedRScript(UriInfo uri, String content, boolean async) throws ParseException {
    ROperation rop = new RestrictedAssignmentROperation(getName(), content, newDataShieldContext());
    if (async) {
      String id = getRServerSession().executeAsync(rop);
      return Response.created(getSymbolURI(uri)).entity(id).type(MediaType.TEXT_PLAIN_TYPE).build();
    } else {
      getRServerSession().execute(rop);
      return Response.created(getSymbolURI(uri)).build();
    }
  }

  /**
   * Transitional Datashield set up from using data frames to tibbles.
   *
   * @param path
   * @param rClass
   * @return
   */
  @Override
  protected MagmaAssignROperation.RClass getRClassToApply(String path, String rClass) {
    MagmaAssignROperation.RClass rClassToApply = super.getRClassToApply(path, rClass);
    if (rClassToApply.equals(MagmaAssignROperation.RClass.TIBBLE)
        || (useTibble && rClassToApply.equals(MagmaAssignROperation.RClass.DATA_FRAME))) {
      rClassToApply = MagmaAssignROperation.RClass.TIBBLE_WITH_FACTORS;
    }
    return rClassToApply;
  }

  @Override
  protected ROperation wrapROperation(ROperation rop) {
    if (!(rop instanceof RestrictedROperation)) {
      return new DataShieldROperation(newDataShieldContext(), getName(), rop);
    }
    return super.wrapROperation(rop);
  }

  private DataShieldContext newDataShieldContext() {
    DataShieldProfile profile = (DataShieldProfile) getRServerSession().getProfile();
    return new DataShieldContext(
        profile.getEnvironment(DSMethodType.ASSIGN),
        getRServerSession().getId(),
        profile.getName(),
        datashieldProfileService.getRParserVersionOrDefault(profile),
        MDC.getCopyOfContextMap());
  }

  private void logInit() {
    DataShieldLog.init();
    RServerSession rSession = getRServerSession();
    DataShieldProfile profile = (DataShieldProfile) rSession.getProfile();
    MDC.put("ds_id", rSession.getId());
    MDC.put("ds_profile", profile.getName());
    MDC.put("ds_symbol", getName());
  }

  private class DataShieldROperation implements ROperation {

    private final DataShieldContext context;

    private final String symbol;

    private final ROperation wrapped;

    private DataShieldROperation(DataShieldContext context, String symbol, ROperation wrapped) {
      this.context = context;
      this.symbol = symbol;
      this.wrapped = wrapped;
    }

    @Override
    public void doWithConnection(RServerConnection connection) {
      MDC.put("ds_symbol", symbol);
      context.getContextMap().forEach(MDC::put);
      try {
        wrapped.doWithConnection(connection);
        DataShieldLog.userLog(context, DataShieldLog.Action.ASSIGN, "created symbol '{}' from: '{}'", symbol, wrapped.toString());
      } catch (Exception e) {
        DataShieldLog.userErrorLog(context, DataShieldLog.Action.ASSIGN, "assignment failure from '{}': {}", wrapped.toString(), e.getMessage());
        throw e;
      }
    }
  }
}
