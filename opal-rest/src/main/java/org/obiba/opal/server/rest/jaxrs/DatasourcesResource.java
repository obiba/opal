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

import org.obiba.magma.MagmaEngine;
import org.obiba.opal.server.rest.model.Datasource;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

@Component
@Path("/datasources")
public class DatasourcesResource {

  @GET
  @Produces( { "application/xml", "application/json" })
  public Set<Datasource> getDatasources() {
    Set<Datasource> names = ImmutableSet.copyOf(Iterables.transform(MagmaEngine.get().getDatasources(), new Function<org.obiba.magma.Datasource, Datasource>() {
      @Override
      public Datasource apply(org.obiba.magma.Datasource from) {
        URI dslink = UriBuilder.fromResource(DatasourceResource.class).path(DatasourceResource.class, "get").build(from.getName());
        return new Datasource(dslink, from);
      }
    }));
    return names;
  }

}
