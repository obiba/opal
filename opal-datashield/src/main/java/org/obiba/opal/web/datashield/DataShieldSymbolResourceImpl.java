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
import org.obiba.opal.datashield.DataShieldLog;
import org.obiba.opal.datashield.RestrictedAssignmentROperation;
import org.obiba.opal.datashield.cfg.DatashieldProfileService;
import org.obiba.opal.r.magma.MagmaAssignROperation;
import org.obiba.opal.spi.r.ROperation;
import org.obiba.opal.web.r.AbstractRSymbolResourceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

@Component("dataShieldSymbolResource")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class DataShieldSymbolResourceImpl extends AbstractRSymbolResourceImpl implements DataShieldSymbolResource {

  @Autowired
  private DatashieldProfileService datashieldProfileService;

  @Value("#{new Boolean('${org.obiba.opal.datashield.useTibble}')}")
  private boolean useTibble;

  @Override
  public Response putTable(UriInfo uri, String path, String variableFilter, Boolean withMissings, String idName, String identifiersMapping, String rClass, boolean async) {
    DataShieldLog.userLog("creating symbol '{}' from opal data '{}'", getName(), path);
    return super.putTable(uri, path, variableFilter, withMissings, idName, identifiersMapping, rClass, async);
  }

  @Override
  public Response putResource(UriInfo uri, String path, boolean async) {
    DataShieldLog.userLog("creating symbol '{}' from opal resource '{}'", getName(), path);
    return super.putResource(uri, path, async);
  }

  @Override
  public Response putRScript(UriInfo uri, String script, boolean async) {
    DataShieldLog.userLog("creating symbol '{}' from R script '{}'", getName(), script);
    return putRestrictedRScript(uri, script, async);
  }

  @Override
  public Response putString(UriInfo uri, String content, boolean async) {
    DataShieldLog.userLog("creating text symbol '{}' as '{}'", getName(), content);
    return super.putString(uri, content, async);
  }

  @Override
  public Response rm() {
    DataShieldLog.userLog("deleting symbol '{}'", getName());
    return super.rm();
  }

  @Override
  public Response getSymbolBinary() {
    return Response.status(Status.FORBIDDEN).build();
  }

  @Override
  public Response getSymbolJSON() {
    return Response.status(Status.FORBIDDEN).build();
  }

  protected Response putRestrictedRScript(UriInfo uri, String content, boolean async) {
    try {
      ROperation rop = new RestrictedAssignmentROperation(getName(), content,
          datashieldProfileService.getProfile(getRServerSession().getProfile()).getEnvironment(DSMethodType.ASSIGN));
      if (async) {
        String id = getRServerSession().executeAsync(rop);
        return Response.created(getSymbolURI(uri)).entity(id).type(MediaType.TEXT_PLAIN_TYPE).build();
      } else {
        getRServerSession().execute(rop);
        return Response.created(getSymbolURI(uri)).build();
      }
    } catch (ParseException e) {
      return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
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
}
