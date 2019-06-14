/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.r;

import com.google.common.base.Strings;
import org.obiba.opal.core.service.DataExportService;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.r.StringAssignROperation;
import org.obiba.opal.r.magma.MagmaAssignROperation;
import org.obiba.opal.r.service.OpalRSession;
import org.obiba.opal.spi.r.ROperation;
import org.obiba.opal.spi.r.RScriptROperation;
import org.springframework.transaction.support.TransactionTemplate;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

/**
 * Handles web services on the symbols of the current R session of the invoking Opal user. A current R session must be
 * defined, otherwise the web service calls will fail with a 404 status.
 */
public abstract class AbstractRSymbolResourceImpl implements RSymbolResource {

  private String name;

  private OpalRSession rSession;

  @NotNull
  protected IdentifiersTableService identifiersTableService;

  @NotNull
  protected DataExportService dataExportService;

  protected TransactionTemplate transactionTemplate;

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public void setOpalRSession(OpalRSession rSession) {
    this.rSession = rSession;
  }

  protected OpalRSession getRSession() {
    return rSession;
  }

  @Override
  public void setIdentifiersTableService(IdentifiersTableService identifiersTableService) {
    this.identifiersTableService = identifiersTableService;
  }

  @Override
  public void setDataExportService(DataExportService dataExportService) {
    this.dataExportService = dataExportService;
  }

  @Override
  public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
    this.transactionTemplate = transactionTemplate;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Response getSymbol() {
    return RSessionResourceHelper.executeScript(rSession, name);
  }

  @Override
  public Response putString(UriInfo uri, String content, boolean async) {
    return assignSymbol(uri, new StringAssignROperation(name, content), async);
  }

  @Override
  public Response putRScript(UriInfo uri, String script, boolean async) {
    RScriptROperation rop = new RScriptROperation(String.format("is.null(base::assign('%s', %s))", name, script));
    rop.setIgnoreResult(true);
    return assignSymbol(uri, rop, async);
  }

  @Override
  public Response putMagma(UriInfo uri, String path, String variableFilter, Boolean withMissings,
                           String idName, String updatedName, String identifiersMapping,
                           String rClass, boolean async) {
    return assignMagmaSymbol(uri, path, variableFilter, withMissings, idName, updatedName, identifiersMapping, rClass, async);
  }

  @Override
  public Response rm() {
    rSession.execute(new RScriptROperation("base::rm(" + name + ")"));
    return Response.ok().build();
  }

  Response assignSymbol(UriInfo uri, ROperation rop, boolean async) {
    if (async) {
      String id = rSession.executeAsync(rop);
      return Response.created(getSymbolURI(uri)).entity(id).type(MediaType.TEXT_PLAIN_TYPE).build();
    } else {
      rSession.execute(rop);
      return Response.created(getSymbolURI(uri)).build();
    }
  }

  Response assignMagmaSymbol(UriInfo uri, String path, String variableFilter, Boolean withMissings,
                             String idName, String updatedName, String identifiersMapping, String rClass,
                             boolean async) {
    MagmaAssignROperation.RClass rClassToApply = getRClassToApply(path, rClass);
    return assignSymbol(uri,
        new MagmaAssignROperation(name, path, variableFilter, withMissings, idName, updatedName, identifiersMapping,
            rClassToApply, identifiersTableService, dataExportService, transactionTemplate), async);
  }

  protected URI getSymbolURI(UriInfo info) {
    return info.getRequestUri();
  }

  protected MagmaAssignROperation.RClass getRClassToApply(String path, String rClass) {
    MagmaAssignROperation.RClass rClassToApply;
    if (path.contains(":"))
      rClassToApply = MagmaAssignROperation.RClass.VECTOR;
    else if (Strings.isNullOrEmpty(rClass))
      rClassToApply = MagmaAssignROperation.RClass.TIBBLE;
    else if ("data.frame".equals(rClass))
      rClassToApply = MagmaAssignROperation.RClass.DATA_FRAME;
    else {
      try {
        rClassToApply = MagmaAssignROperation.RClass.valueOf(rClass.toUpperCase());
      } catch (Exception e) {
        rClassToApply = MagmaAssignROperation.RClass.TIBBLE;
      }
    }
    return rClassToApply;
  }

}
