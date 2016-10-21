/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.r;

import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.r.MagmaAssignROperation;
import org.obiba.opal.r.ROperation;
import org.obiba.opal.r.RScriptROperation;
import org.obiba.opal.r.StringAssignROperation;
import org.obiba.opal.r.service.OpalRSession;

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
  private IdentifiersTableService identifiersTableService;

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
    RScriptROperation rop = new RScriptROperation(name + " <- " + script);
    rop.setIgnoreResult(true);
    return assignSymbol(uri, rop, async);
  }

  @Override
  public Response putMagma(UriInfo uri, String path, String variableFilter, Boolean withMissings, Boolean withIdentifiers, Boolean withTimestamps, String identifiersMapping,
                           boolean async) {
    boolean wIds = withIdentifiers == null ? withIdColumn() : withIdentifiers;
    boolean wTs = withTimestamps == null ? withTimestampsColumns() : withTimestamps;
    return assignSymbol(uri,
        new MagmaAssignROperation(name, path, variableFilter, withMissings, wIds, wTs, identifiersMapping, identifiersTableService), async);
  }

  @Override
  public Response rm() {
    rSession.execute(new RScriptROperation("base::rm(" + name + ")"));
    return Response.ok().build();
  }

  protected Response assignSymbol(UriInfo uri, ROperation rop, boolean async) {
    if (async) {
      String id = rSession.executeAsync(rop);
      return Response.created(getSymbolURI(uri)).entity(id).type(MediaType.TEXT_PLAIN_TYPE).build();
    } else {
      rSession.execute(rop);
      return Response.created(getSymbolURI(uri)).build();
    }
  }

  protected URI getSymbolURI(UriInfo info) {
    return info.getRequestUri();
  }

  /**
   * Whether the entity ID is a column in the R data.frame when assigning a {@link org.obiba.magma.ValueTable}.
   *
   * @return
   */
  protected boolean withIdColumn() {
    return true;
  }

  /**
   * Whether the value set timestamps are to be columns in the R data.frame when assigning a {@link org.obiba.magma.ValueTable}.
   *
   * @return
   */
  protected boolean withTimestampsColumns() {
    return false;
  }
}
