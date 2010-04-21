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
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

@Component
@Path("/datasources")
public class DatasourcesResource {

  @OPTIONS
  public Response getOptions(@Context HttpHeaders requestHeaders) {
    System.out.println("bonjour");
    return Response.ok().header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET").header("Access-Control-Allow-Headers", "user-agent").build();
  }

  @GET
  @Produces("application/xml")
  public Set<String> getDatasources() {
    Set<String> names = ImmutableSet.copyOf(Iterables.transform(MagmaEngine.get().getDatasources(), new Function<Datasource, String>() {
      @Override
      public String apply(Datasource from) {
        return from.getName();
      }
    }));
    return names;
  }

}
