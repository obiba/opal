/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.server.rest.jaxrs;

import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.sun.jersey.api.core.ResourceContext;

@Component("jersey.datasourceResource")
@Path("/datasource")
public class DatasourceResource {

  private static final Logger log = LoggerFactory.getLogger(DatasourceResource.class);

  @Context
  private ResourceContext resourceContext;

  @GET
  @Path("/{name}/tables")
  @Produces("application/xml")
  public Set<String> getDatasource(@PathParam("name") String name) {
    Set<String> names = ImmutableSet.copyOf(Iterables.transform(MagmaEngine.get().getDatasource(name).getValueTables(), new Function<ValueTable, String>() {
      @Override
      public String apply(ValueTable from) {
        return from.getName();
      }
    }));
    return names;
  }

  @Path("/{name}/{table}")
  public TableResource getTableResource(@PathParam("name") String name, @PathParam("table") String table) {
    TableResource resource = resourceContext.getResource(TableResource.class);
    resource.setValueTable(MagmaEngine.get().getDatasource(name).getValueTable(table));
    return resource;
  }
}
