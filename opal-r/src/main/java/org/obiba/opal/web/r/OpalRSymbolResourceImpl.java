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

import org.apache.shiro.SecurityUtils;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.MagmaEngineReferenceResolver;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.support.MagmaEngineVariableResolver;
import org.obiba.opal.r.DataAssignROperation;
import org.obiba.opal.r.MagmaRRuntimeException;
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
  public Response putMagma(UriInfo uri, String path, String variableFilter, Boolean withMissings, String idName, String updatedName, String identifiersMapping,
                           boolean async) {
    if (path == null) return Response.status(Response.Status.BAD_REQUEST).build();

    ValueTable table = getValueTable(path);
    if (table == null) return Response.status(Response.Status.BAD_REQUEST).build();
    if (!areValueSetReadable(table)) return Response.status(Response.Status.FORBIDDEN).build();

    return super.putMagma(uri, path, variableFilter, withMissings, idName, updatedName, identifiersMapping, async);
  }

  @Override
  public Response putRData(@Context UriInfo uri, String content, @DefaultValue("false") boolean async) {
    DataAssignROperation rop = new DataAssignROperation(getName(), content);
    return assignSymbol(uri, rop, async);
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
