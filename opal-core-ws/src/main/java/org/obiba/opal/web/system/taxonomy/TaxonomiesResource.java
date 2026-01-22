/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.apache.commons.vfs2.FileSystemException;
import org.obiba.opal.core.cfg.TaxonomyService;
import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.web.BaseResource;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Opal.TaxonomyDto;
import org.obiba.opal.web.taxonomy.Dtos;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

@Component
@Path("/system/conf/taxonomies")
@Tag(name = "Taxonomies", description = "Operations on taxonomies")
public class TaxonomiesResource implements BaseResource {

  @Autowired
  private TaxonomyService taxonomyService;

  @GET
  @NoAuthorization
  @Operation(summary = "Get all taxonomies", description = "Retrieves all available taxonomies.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Taxonomies retrieved successfully")
  })
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
  @Operation(summary = "Get taxonomy summaries", description = "Retrieves summary information for all taxonomies.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Taxonomy summaries retrieved successfully")
  })
  public Opal.TaxonomiesDto getTaxonomySummaries() {
    Opal.TaxonomiesDto.Builder builder = Opal.TaxonomiesDto.newBuilder();
    for(Taxonomy taxonomy : taxonomyService.getTaxonomies()) {
      builder.addSummaries(Dtos.asSummaryDto(taxonomy));
    }
    return builder.build();
  }

  @GET
  @Path("tags/_github")
  @Operation(
    summary = "Get GitHub taxonomy tags",
    description = "Retrieves the available tags from a GitHub repository containing taxonomies. Useful for discovering available taxonomy versions from remote repositories."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "GitHub taxonomy tags successfully retrieved"),
    @ApiResponse(responseCode = "400", description = "Invalid GitHub repository or access denied"),
    @ApiResponse(responseCode = "404", description = "GitHub repository or tags not found"),
    @ApiResponse(responseCode = "500", description = "Error accessing GitHub API")
  })
  public Opal.VcsTagsInfoDto getTaxonomyGitHubTags(
      @QueryParam("user") @DefaultValue("maelstrom-research") String username, @QueryParam("repo") String repo) {
    
    return org.obiba.opal.web.magma.vcs.Dtos.asDto(taxonomyService.getGitHubTaxonomyTags(username, repo));
  }

  @POST
  @Path("import/_github")
  @Operation(
    summary = "Import taxonomy from GitHub",
    description = "Imports taxonomies from a GitHub repository. Can import all taxonomies from a repository or a specific taxonomy file. Supports version control through git references (branch, tag, commit)."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Taxonomy successfully imported from GitHub"),
    @ApiResponse(responseCode = "400", description = "Invalid GitHub repository, file not found, or import conflict"),
    @ApiResponse(responseCode = "404", description = "GitHub repository or file not found"),
    @ApiResponse(responseCode = "500", description = "Error importing from GitHub or processing taxonomy file")
  })
  public Response importTaxonomyFromGitHub(@Context UriInfo uriInfo,
      @QueryParam("user") @DefaultValue("maelstrom-research") String username, @QueryParam("repo") String repo,
      @QueryParam("ref") @DefaultValue("master") String ref,
      @QueryParam("file") String file,
      @QueryParam("override") @DefaultValue("false") boolean override) {

    if (Strings.isNullOrEmpty(file)) {
      List<Taxonomy> taxonomies = taxonomyService.importGitHubTaxonomies(username, repo, ref, override);
      if(override && taxonomies.isEmpty()) return Response.status(Response.Status.BAD_REQUEST).build();
      URI uri = uriInfo.getBaseUriBuilder().path("/system/conf/taxonomies").build();
      return Response.created(uri).build();
    }

    Taxonomy taxonomy = taxonomyService.importGitHubTaxonomy(username, repo, ref, file, override);
    if(taxonomy == null) return Response.status(Response.Status.BAD_REQUEST).build();
    URI uri = uriInfo.getBaseUriBuilder().path("/system/conf/taxonomy").path(taxonomy.getName()).build();
    return Response.created(uri).build();
  }

  @POST
  @Path("import/_file")
  @Operation(
    summary = "Import taxonomy from file",
    description = "Imports a taxonomy from a local file system path. Supports various taxonomy file formats. Can override existing taxonomy with the same name if explicitly requested."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Taxonomy successfully imported from file"),
    @ApiResponse(responseCode = "400", description = "File path not provided, file not found, or invalid taxonomy format"),
    @ApiResponse(responseCode = "409", description = "Taxonomy with same name already exists and override not specified"),
    @ApiResponse(responseCode = "500", description = "Error reading or processing taxonomy file")
  })
  public Response importTaxonomyFromFile(@Context UriInfo uriInfo, @QueryParam("file") String file,
                                         @QueryParam("override") @DefaultValue("false") boolean override) throws
      FileSystemException {
    if(Strings.isNullOrEmpty(file)) return Response.status(Response.Status.BAD_REQUEST).build();
    Taxonomy taxonomy = taxonomyService.importFileTaxonomy(file, override);
    if(taxonomy == null) return Response.status(Response.Status.BAD_REQUEST).build();

    URI uri = uriInfo.getBaseUriBuilder().path("/system/conf/taxonomy").path(taxonomy.getName()).build();
    return Response.created(uri).build();
  }

  @POST
  @Operation(
    summary = "Create taxonomy",
    description = "Creates a new taxonomy in the system. The taxonomy definition includes vocabulary and term structures. Taxonomy names must be unique within the system."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Taxonomy successfully created"),
    @ApiResponse(responseCode = "400", description = "Invalid taxonomy data or missing required fields"),
    @ApiResponse(responseCode = "409", description = "Taxonomy with same name already exists"),
    @ApiResponse(responseCode = "500", description = "Error creating taxonomy")
  })
  public Response addTaxonomy(@Context UriInfo uriInfo, TaxonomyDto dto) {
    taxonomyService.ensureUniqueTaxonomy(dto.getName());
    taxonomyService.saveTaxonomy(Dtos.fromDto(dto));
    URI uri = uriInfo.getBaseUriBuilder().path("/system/conf/taxonomy").path(dto.getName()).build();
    return Response.created(uri).build();
  }

}

