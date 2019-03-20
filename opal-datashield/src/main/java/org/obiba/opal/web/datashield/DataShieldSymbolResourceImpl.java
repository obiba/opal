/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.datashield;

import com.google.common.base.Supplier;
import org.obiba.datashield.core.DSMethodType;
import org.obiba.datashield.r.expr.ParseException;
import org.obiba.opal.datashield.DataShieldLog;
import org.obiba.opal.datashield.RestrictedAssignmentROperation;
import org.obiba.opal.datashield.cfg.DatashieldConfiguration;
import org.obiba.opal.spi.r.ROperation;
import org.obiba.opal.web.r.AbstractRSymbolResourceImpl;
import org.springframework.beans.factory.annotation.Autowired;
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
  private Supplier<DatashieldConfiguration> configSupplier;

  @Override
  public Response putMagma(UriInfo uri, String path, String variableFilter, Boolean withMissings, String idName,
                           String updatedName, String identifiersMapping, String rClass,
                           boolean async) {
    DataShieldLog.userLog("creating symbol '{}' from opal data '{}'", getName(), path);
    return super.putMagma(uri, path, variableFilter, withMissings, null, null, identifiersMapping, rClass, async);
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
  public Response getSymbol() {
    return Response.noContent().build();
  }

  protected Response putRestrictedRScript(UriInfo uri, String content, boolean async) {
    try {
      ROperation rop = new RestrictedAssignmentROperation(getName(), content,
          configSupplier.get().getEnvironment(DSMethodType.ASSIGN));
      if(async) {
        String id = getRSession().executeAsync(rop);
        return Response.created(getSymbolURI(uri)).entity(id).type(MediaType.TEXT_PLAIN_TYPE).build();
      } else {
        getRSession().execute(rop);
        return Response.created(getSymbolURI(uri)).build();
      }
    } catch(ParseException e) {
      return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
    }
  }
}
