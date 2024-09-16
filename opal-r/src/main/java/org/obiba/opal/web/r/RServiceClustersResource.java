/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.r;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.obiba.opal.r.service.RServerManagerService;
import org.obiba.opal.web.model.OpalR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope("request")
@Path("/service/r/clusters")
public class RServiceClustersResource {

  @Autowired
  private RServerManagerService rServerManagerService;

  @GET
  public List<OpalR.RServerClusterDto> getClusters() {
    return rServerManagerService.getRServerClusters().stream()
        .map(Dtos::asDto)
        .sorted(Comparator.comparing(OpalR.RServerClusterDto::getName))
        .collect(Collectors.toList());
  }

  @DELETE
  @Path("/cache")
  public Response evictCache() {
    rServerManagerService.evictCache();
    return Response.ok().build();
  }
}
