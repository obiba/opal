/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.r;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.shiro.SecurityUtils;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.MagmaEngineReferenceResolver;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.support.MagmaEngineVariableResolver;
import org.obiba.opal.r.MagmaRRuntimeException;
import org.obiba.opal.r.service.OpalRSession;

import com.google.common.collect.ImmutableSet;

/**
 */
public class SecuredRSymbolResource extends RSymbolResource {

  public SecuredRSymbolResource(OpalRSession rSession, String name) {
    super(rSession, name);
  }

  /**
   * Only user with permission to see the values of a table/variable can push the data to R.
   *
   * @param uri
   * @param path
   * @return
   */
  @Override
  public Response putMagma(@Context UriInfo uri, String path) {
    if(path == null) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }

    for(ValueTable table : getValueTables(path)) {
      if(!areValueSetReadable(table)) {
        return Response.status(Response.Status.FORBIDDEN).build();
      }
    }

    return super.putMagma(uri, path);
  }

  private Iterable<ValueTable> getValueTables(String path) {
    if(!path.contains(".") && !path.contains(":")) {
      return MagmaEngine.get().getDatasource(path).getValueTables();
    }
    return path.contains(".") && !path.contains(":")
        ? ImmutableSet.of(getValueTable(path, MagmaEngineTableResolver.valueOf(path)))
        : ImmutableSet.of(getValueTable(path, MagmaEngineVariableResolver.valueOf(path)));
  }

  private ValueTable getValueTable(String path, MagmaEngineReferenceResolver resolver) {
    if(resolver.getDatasourceName() == null) {
      throw new MagmaRRuntimeException("Datasource is not defined in path: " + path);
    }
    Datasource ds = MagmaEngine.get().getDatasource(resolver.getDatasourceName());

    return ds.getValueTable(resolver.getTableName());
  }

  private boolean areValueSetReadable(ValueTable valueTable) {
    return SecurityUtils.getSubject().isPermitted("magma:/datasource/" + valueTable.getDatasource().getName() +
        "/table/" + valueTable.getName() + "/valueSet:GET");
  }
}
