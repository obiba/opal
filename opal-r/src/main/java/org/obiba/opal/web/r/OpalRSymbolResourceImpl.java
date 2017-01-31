/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.r;

import com.google.common.base.Strings;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.shiro.SecurityUtils;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.MagmaEngineReferenceResolver;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.support.MagmaEngineVariableResolver;
import org.obiba.opal.r.DataAssignROperation;
import org.obiba.opal.r.DataSaveROperation;
import org.obiba.opal.r.MagmaRRuntimeException;
import org.obiba.opal.r.RScriptROperation;
import org.obiba.opal.r.magma.MagmaAssignROperation;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Component("opalRSymbolResource")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class OpalRSymbolResourceImpl extends AbstractRSymbolResourceImpl implements OpalRSymbolResource {

  @Override
  public Response putMagma(UriInfo uri, String path, String variableFilter, Boolean withMissings, String idName,
                           String updatedName, String identifiersMapping, boolean async) {
    Response check = checkValueTable(path);
    if (check != null) return check;
    return super.putMagma(uri, path, variableFilter, withMissings, idName, updatedName, identifiersMapping, async);
  }

  @Override
  public Response putRData(@Context UriInfo uri, String content, @DefaultValue("false") boolean async) {
    DataAssignROperation rop = new DataAssignROperation(getName(), content);
    return assignSymbol(uri, rop, async);
  }

  @Override
  public Response importMagma(String project) {
    RScriptROperation rop = new RScriptROperation(getName(), false);
    getRSession().execute(rop);
    return Response.status(rop.hasResult() ? Response.Status.OK : Response.Status.BAD_REQUEST).build();
  }

  @Override
  public Response exportMagma(@Context UriInfo uri, String path, String variableFilter, String idName,
                              String updatedName, String identifiersMapping, boolean async, String destination) {
    Response check = checkValueTable(path);
    if (check != null) return check;
    return assignMagmaSymbol(uri, path, variableFilter, true, idName, updatedName, identifiersMapping,
        MagmaAssignROperation.RClass.TIBBLE,async);
  }

  @Override
  public Response saveRData(String destination) {
    // destination must be relative
    if (!Strings.isNullOrEmpty(destination) &&
        (destination.startsWith("~") || destination.startsWith("/") || destination.startsWith("$")))
      return Response.status(Response.Status.BAD_REQUEST) //
          .entity("Destination file must be relative to R workspace.").build();
    DataSaveROperation rop = new DataSaveROperation(getName(), destination);
    getRSession().execute(rop);
    return Response.ok().build();
  }

  //
  // Private methods
  //

  private Response checkValueTable(String path) {
    if (path == null) return Response.status(Response.Status.BAD_REQUEST).build();

    ValueTable table = getValueTable(path);
    if (table == null) return Response.status(Response.Status.BAD_REQUEST).build();
    if (!areValueSetReadable(table)) return Response.status(Response.Status.FORBIDDEN).build();

    return null;
  }

  private ValueTable getValueTable(String path) {
    if (!path.contains(".")) return null;
    return path.contains(":")
        ? getValueTable(path, MagmaEngineVariableResolver.valueOf(path))
        : getValueTable(path, MagmaEngineTableResolver.valueOf(path));
  }

  private ValueTable getValueTable(String path, MagmaEngineReferenceResolver resolver) {
    if (resolver.getDatasourceName() == null) {
      throw new MagmaRRuntimeException("Datasource is not defined in path: " + path);
    }
    Datasource ds = MagmaEngine.get().getDatasource(resolver.getDatasourceName());
    return ds.getValueTable(resolver.getTableName());
  }

  private boolean areValueSetReadable(ValueTable valueTable) {
    return SecurityUtils.getSubject().isPermitted("rest:/datasource/" + valueTable.getDatasource().getName() +
        "/table/" + valueTable.getName() + "/valueSet:GET");
  }

}
