/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.system.taxonomy;

import java.net.URI;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.obiba.opal.core.cfg.TaxonomyService;
import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Opal.TaxonomyDto;
import org.obiba.opal.web.taxonomy.Dtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/system/conf/taxonomies")
public class TaxonomiesResource {

  @Autowired
  private TaxonomyService taxonomyService;

  @GET
  public Opal.TaxonomiesDto getTaxonomies() {
    Opal.TaxonomiesDto.Builder builder = Opal.TaxonomiesDto.newBuilder();
    for(Taxonomy taxonomy : taxonomyService.getTaxonomies()) {
      builder.addSummaries(Dtos.asSummaryDto(taxonomy));
    }
    return builder.build();
  }

  @POST
  @Path("_import_github")
  public Response importTaxonomy(@Context UriInfo uriInfo,
      @QueryParam("user") @DefaultValue("maelstrom-research") String username, @QueryParam("repo") String repo,
      @QueryParam("ref") @DefaultValue("master") String ref,
      @QueryParam("file") @DefaultValue("taxonomy.yml") String file) {
    Taxonomy taxonomy = taxonomyService.importGitHubTaxonomy(username, repo, ref, file);
    if(taxonomy == null) return Response.status(Response.Status.BAD_REQUEST).build();

    URI uri = uriInfo.getBaseUriBuilder().path("/system/conf/taxonomy").path(taxonomy.getName()).build();
    return Response.created(uri).build();
  }

  @POST
  public Response addOrUpdateTaxonomy(@Context UriInfo uriInfo, TaxonomyDto dto) {
    boolean update = taxonomyService.hasTaxonomy(dto.getName());
    taxonomyService.saveTaxonomy(Dtos.fromDto(dto));

    if (update) return Response.ok().build();

    URI uri = uriInfo.getBaseUriBuilder().path("/system/conf/taxonomy").path(dto.getName()).build();
    return Response.created(uri).build();
  }
}

