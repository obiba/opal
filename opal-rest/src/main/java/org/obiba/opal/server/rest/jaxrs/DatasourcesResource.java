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

import java.net.URI;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.UriBuilder;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.opal.web.model.OpalModel;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

@Component
@Path("/datasources")
public class DatasourcesResource {

  @GET
  @Produces( { "application/json" })
  public Set<OpalModel.DatasourceDTO> getDatasources() {
    Set<OpalModel.DatasourceDTO> names = ImmutableSet.copyOf(Iterables.transform(MagmaEngine.get().getDatasources(), new Function<Datasource, OpalModel.DatasourceDTO>() {
      @Override
      public OpalModel.DatasourceDTO apply(Datasource from) {
        URI dslink = UriBuilder.fromResource(DatasourceResource.class).path(DatasourceResource.class, "get").build(from.getName());
        OpalModel.DatasourceDTO.Builder ds = OpalModel.DatasourceDTO.newBuilder().setName(from.getName()).setLink(dslink.toString());
        for(ValueTable table : from.getValueTables()) {
          ds.addTable(table.getName());
        }
        return ds.build();
      }
    }));

    return names;
  }
}
