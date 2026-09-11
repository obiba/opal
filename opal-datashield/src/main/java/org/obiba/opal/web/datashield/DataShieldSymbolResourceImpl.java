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
import io.opentelemetry.context.Scope;
import org.obiba.datashield.core.DSMethodType;
import org.obiba.datashield.r.expr.ParseException;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.js.views.JavascriptClause;
import org.obiba.magma.support.MagmaEngineReferenceResolver;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.support.MagmaEngineVariableResolver;
import org.obiba.opal.datashield.DataShieldContext;
import org.obiba.opal.datashield.DataShieldLog;
import org.obiba.opal.datashield.DataShieldTracer;
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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;

@Component("dataShieldSymbolResource")
@org.springframework.context.annotation.Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class DataShieldSymbolResourceImpl extends AbstractRSymbolResourceImpl implements DataShieldSymbolResource {

  @Autowired
  private DataShieldProfileService datashieldProfileService;

  @Value("#{new Boolean('${datashield.useTibble}')}")
  private boolean useTibble;

  /**
   * A table assignment is checked here, on the request thread, before anything is queued: the table
   * must exist, the user must be allowed to read it - which is the same lookup, the datasource being
   * secured - and the variable filter must compile. The operation that runs on the consumer thread
   * resolves the table again, cheaply; what matters is that a refusal never gets that far, and is
   * recorded as one.
   */
  @Override
  public Response putTable(UriInfo uri, String path, String variableFilter, Boolean withMissings, String idName,
      String identifiersMapping, String rClass, boolean async) {
    logInit();
    MDC.put("ds_table", path);
    DataShieldContext context = newDataShieldContext();
    MagmaAssignROperation rop = new MagmaAssignROperation(getName(), path, variableFilter, withMissings, idName,
        identifiersMapping, getRClassToApply(path, rClass), identifiersTableService, dataExportService, rCacheHelper);
    DataShieldTracer.Operation operation = DataShieldTracer.begin(context, DataShieldLog.Action.ASSIGN, getName(),
        rop.toString());
    try (Scope ignored = operation.makeCurrent()) {
      operation.resolve("datashield.table", path, () -> resolveTable(path, variableFilter));
      DataShieldLog.userLog(context, DataShieldLog.Action.RESOLVE, "resolved table '{}'", path);
    } catch (RuntimeException e) {
      DataShieldLog.userErrorLog(context, DataShieldLog.Action.RESOLVE, "refused table '{}': {}", path, e.getMessage());
      throw e;
    }
    return execute(uri, new DataShieldROperation(context, getName(), rop, operation), async);
  }

  /**
   * As {@link #putTable}: the reference is looked up - existence and permission - before the
   * assignment is queued, and the lookup is what the trace and the audit log record.
   */
  @Override
  public Response putResource(UriInfo uri, String path, boolean async) {
    logInit();
    MDC.put("ds_resource", path);
    DataShieldContext context = newDataShieldContext();
    int idx = path.indexOf(".");
    String project = idx < 0 ? path : path.substring(0, idx);
    String name = idx < 0 ? "" : path.substring(idx + 1);
    DataShieldTracer.Operation operation = DataShieldTracer.begin(context, DataShieldLog.Action.ASSIGN, getName(),
        String.format("%s <- resource[%s]", getName(), path));
    ROperation rop;
    try (Scope ignored = operation.makeCurrent()) {
      rop = operation.resolve("datashield.resource", path, () -> {
        // ensures the resource exists and the user may use it
        getResourceReferenceService().getResourceReference(project, name);
        return getResourceReferenceService().asAssignOperation(project, name, getName());
      });
      DataShieldLog.userLog(context, DataShieldLog.Action.RESOLVE, "resolved resource '{}'", path);
    } catch (RuntimeException e) {
      DataShieldLog.userErrorLog(context, DataShieldLog.Action.RESOLVE, "refused resource '{}': {}", path, e.getMessage());
      throw e;
    }
    return execute(uri, new DataShieldROperation(context, getName(), rop, operation), async);
  }

  @Override
  public Response putRScript(UriInfo uri, String script, boolean async) throws Exception {
    logInit();
    return putRestrictedRScript(uri, script, async);
  }

  /**
   * A text/plain body is sent to the R server as a script, exactly like an application/x-rscript one: it goes through
   * the same restricted parser. The interface declares no checked exception on this method, so a rejected script is
   * turned into the response the {@link ParseExceptionMapper} would have produced.
   */
  @Override
  public Response putString(UriInfo uri, String content, boolean async) {
    logInit();
    try {
      return putRestrictedRScript(uri, content, async);
    } catch (ParseException e) {
      return new ParseExceptionMapper().toResponse(e);
    }
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
    return execute(uri, new RestrictedAssignmentROperation(getName(), content, newDataShieldContext()), async);
  }

  private Response execute(UriInfo uri, ROperation rop, boolean async) {
    if (async) {
      String id = getRServerSession().executeAsync(rop);
      return Response.created(getSymbolURI(uri)).entity(id).type(MediaType.TEXT_PLAIN_TYPE).build();
    } else {
      getRServerSession().execute(rop);
      return Response.created(getSymbolURI(uri)).build();
    }
  }

  /**
   * What {@code MagmaAssignROperation} will do on the consumer thread, done first on the request thread
   * so that what it refuses is refused before it is queued.
   */
  private ValueTable resolveTable(String path, String variableFilter) {
    MagmaEngineReferenceResolver resolver = path.contains(":")
        ? MagmaEngineVariableResolver.valueOf(path)
        : MagmaEngineTableResolver.valueOf(path);
    if (Strings.isNullOrEmpty(resolver.getDatasourceName()))
      throw new IllegalArgumentException("Datasource is not defined in path: " + path);
    Datasource datasource = MagmaEngine.get().getDatasource(resolver.getDatasourceName());
    ValueTable table = datasource.getValueTable(resolver.getTableName());
    // a datasource is meant to throw for a table it does not have; not all of them do
    if (table == null) throw new NoSuchValueTableException(resolver.getDatasourceName(), resolver.getTableName());
    if (!Strings.isNullOrEmpty(resolver.getVariableName())) table.getVariable(resolver.getVariableName());
    if (!Strings.isNullOrEmpty(variableFilter)) new JavascriptClause(variableFilter).initialise();
    return table;
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

  /**
   * Only three kinds of assignment may reach a DataSHIELD session: a script rewritten by the restricted parser, a
   * table and a resource. The table and the resource are checked and queued by {@link #putTable} and
   * {@link #putResource} without coming through here, so anything else that does carries R code the parser never
   * saw, whatever inherited method built it, and is refused rather than trusted to the method that built it.
   */
  @Override
  protected ROperation wrapROperation(ROperation rop) {
    if (rop instanceof RestrictedROperation) {
      return super.wrapROperation(rop);
    }
    DataShieldLog.userErrorLog(newDataShieldContext(), DataShieldLog.Action.ASSIGN, "refused unrestricted assignment of '{}'", getName());
    throw new ForbiddenException("Unrestricted R operations are not allowed in a DataSHIELD session");
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

  /**
   * The evaluation half of a table or resource assignment: ends the operation its check began.
   */
  private static class DataShieldROperation implements ROperation {

    private final DataShieldContext context;

    private final String symbol;

    private final ROperation wrapped;

    private final DataShieldTracer.Operation operation;

    private DataShieldROperation(DataShieldContext context, String symbol, ROperation wrapped,
        DataShieldTracer.Operation operation) {
      this.context = context;
      this.symbol = symbol;
      this.wrapped = wrapped;
      this.operation = operation;
    }

    @Override
    public void doWithConnection(RServerConnection connection) {
      MDC.put("ds_symbol", symbol);
      context.getContextMap().forEach(MDC::put);
      // once: the span, the audit record and the error record must describe the same thing
      String script = wrapped.toString();
      try (Scope ignored = operation.makeCurrent()) {
        operation.evaluate(script, () -> wrapped.doWithConnection(connection));
        DataShieldLog.userLog(context, DataShieldLog.Action.ASSIGN, "created symbol '{}' from: '{}'", symbol, script);
      } catch (Exception e) {
        DataShieldLog.userErrorLog(context, DataShieldLog.Action.ASSIGN, "assignment failure from '{}': {}", script, e.getMessage());
        throw e;
      }
    }

    @Override
    public String toString() {
      return wrapped.toString();
    }
  }
}
