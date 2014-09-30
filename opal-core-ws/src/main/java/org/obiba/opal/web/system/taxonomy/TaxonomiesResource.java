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
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.vfs2.FileSystemException;
import org.obiba.opal.core.cfg.TaxonomyService;
import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Opal.TaxonomyDto;
import org.obiba.opal.web.taxonomy.Dtos;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

@Component
@Path("/system/conf/taxonomies")
public class TaxonomiesResource {

  @Autowired
  private TaxonomyService taxonomyService;

  @GET
  @NoAuthorization
  public List<TaxonomyDto> getTaxonomies() {
    List<TaxonomyDto> taxonomies = new ArrayList<>();
    for(Taxonomy taxonomy : taxonomyService.getTaxonomies()) {
      taxonomies.add(Dtos.asDto(taxonomy));
    }
    return taxonomies;
  }

  @GET
  @Path("summaries")
  @NoAuthorization
  public Opal.TaxonomiesDto getTaxonomySummaries() {
    Opal.TaxonomiesDto.Builder builder = Opal.TaxonomiesDto.newBuilder();
    for(Taxonomy taxonomy : taxonomyService.getTaxonomies()) {
      builder.addSummaries(Dtos.asSummaryDto(taxonomy));
    }
    return builder.build();
  }

  @POST
  @Path("import/_github")
  public Response importTaxonomyFromGitHub(@Context UriInfo uriInfo,
      @QueryParam("user") @DefaultValue("maelstrom-research") String username, @QueryParam("repo") String repo,
      @QueryParam("ref") @DefaultValue("master") String ref,
      @QueryParam("file") @DefaultValue("taxonomy.yml") String file) {
    Taxonomy taxonomy = taxonomyService.importGitHubTaxonomy(username, repo, ref, file);
    if(taxonomy == null) return Response.status(Response.Status.BAD_REQUEST).build();

    URI uri = uriInfo.getBaseUriBuilder().path("/system/conf/taxonomy").path(taxonomy.getName()).build();
    return Response.created(uri).build();
  }

  @POST
  @Path("import/_default")
  public Response importDefaultTaxonomies() {
    taxonomyService.importDefault();
    return Response.ok().build();
  }

  @POST
  @Path("import/_file")
  public Response importTaxonomyFromFile(@Context UriInfo uriInfo, @QueryParam("file") String file) throws
      FileSystemException {
    if(Strings.isNullOrEmpty(file)) return Response.status(Response.Status.BAD_REQUEST).build();
    Taxonomy taxonomy = taxonomyService.importFileTaxonomy(file);
    if(taxonomy == null) return Response.status(Response.Status.BAD_REQUEST).build();

    URI uri = uriInfo.getBaseUriBuilder().path("/system/conf/taxonomy").path(taxonomy.getName()).build();
    return Response.created(uri).build();
  }

  @POST
  public Response addOrUpdateTaxonomy(@Context UriInfo uriInfo, TaxonomyDto dto) {
    boolean update = taxonomyService.hasTaxonomy(dto.getName());
    taxonomyService.saveTaxonomy(Dtos.fromDto(dto));

    if(update) return Response.ok().build();

    URI uri = uriInfo.getBaseUriBuilder().path("/system/conf/taxonomy").path(dto.getName()).build();
    return Response.created(uri).build();
  }

}

