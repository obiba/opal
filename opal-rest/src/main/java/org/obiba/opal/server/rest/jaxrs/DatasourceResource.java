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

import org.apache.shiro.SecurityUtils;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.opal.web.model.DatasourceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

@Component
@Path("/datasource")
public class DatasourceResource {

  private static final Logger log = LoggerFactory.getLogger(DatasourceResource.class);

  @GET
  @Path("/{name}")
  @Produces("application/json")
  public DatasourceDTO get(@PathParam("name") String name) {
    Datasource ds = MagmaEngine.get().getDatasource(name);
    DatasourceDTO datasource = new DatasourceDTO(ds.getName());
    for(ValueTable table : ds.getValueTables()) {
      datasource.addTable(table.getName());
    }
    return datasource;
  }

  @GET
  @Path("/{name}/tables")
  @Produces("application/xml")
  public Set<String> getTables(@PathParam("name") String name) {
    Iterable<ValueTable> filteredTables = Iterables.filter(MagmaEngine.get().getDatasource(name).getValueTables(), new Predicate<ValueTable>() {

      @Override
      public boolean apply(ValueTable input) {
        String tableFqn = input.getDatasource().getName() + '.' + input.getName();
        return SecurityUtils.getSubject().isPermitted("tables:" + tableFqn + ":read");
      }

    });
    Set<String> names = ImmutableSet.copyOf(Iterables.transform(filteredTables, new Function<ValueTable, String>() {
      @Override
      public String apply(ValueTable from) {
        return from.getName();
      }
    }));
    return names;
  }

  @Path("/{name}/table/{table}")
  public TableResource getTable(@PathParam("name") String name, @PathParam("table") String table) {
    /*
     * String tableFqn = name + "." + table; try { SecurityUtils.getSubject().checkPermission("tables:" + tableFqn +
     * ":read"); } catch(AuthorizationException e) { log.warn("Unauthorized access to table {}", tableFqn); throw new
     * NoSuchValueTableException("unauthorized: " + tableFqn); }
     */
    return getTableResource(MagmaEngine.get().getDatasource(name).getValueTable(table));
  }

  @Bean
  @Scope("request")
  public TableResource getTableResource(ValueTable table) {
    return new TableResource(table);
  }

}
