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

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import org.obiba.git.CommitInfo;
import org.obiba.opal.core.cfg.NoSuchTaxonomyException;
import org.obiba.opal.core.cfg.TaxonomyService;
import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.core.support.yaml.TaxonomyYaml;
import org.obiba.opal.core.vcs.OpalGitUtils;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.taxonomy.Dtos;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nullable;

@Component
@Scope("request")
@Path("/system/conf/taxonomy/{name}")
@Tag(name = "Taxonomies", description = "Operations on taxonomies")
public class TaxonomyResource {

  @Autowired
  private TaxonomyService taxonomyService;

  @Autowired
  private ApplicationContext applicationContext;

  @PathParam("name")
  private String name;

@GET
@NoAuthorization
@Operation(summary = "Get taxonomy", description = "Retrieve a specific taxonomy with all its vocabularies and terms")
@ApiResponses({
  @ApiResponse(responseCode = "200", description = "Taxonomy successfully retrieved"),
  @ApiResponse(responseCode = "404", description = "Taxonomy not found"),
  @ApiResponse(responseCode = "500", description = "Internal server error")
})
public Opal.TaxonomyDto getTaxonomy() {
    Taxonomy taxonomy = taxonomyService.getTaxonomy(name);
    if(taxonomy == null) throw new NoSuchTaxonomyException(name);
    return Dtos.asDto(taxonomy);
  }

@GET
@Produces(value = "text/plain")
@Path("_download")
@Operation(summary = "Download taxonomy", description = "Download a taxonomy as a YAML file for backup or import")
@ApiResponses({
  @ApiResponse(responseCode = "200", description = "Taxonomy file successfully generated"),
  @ApiResponse(responseCode = "404", description = "Taxonomy not found"),
  @ApiResponse(responseCode = "500", description = "Internal server error")
})
public Response download() {
    Taxonomy taxonomy = taxonomyService.getTaxonomy(name);
    if(taxonomy == null) throw new NoSuchTaxonomyException(name);
    TaxonomyYaml yaml = new TaxonomyYaml();
    return Response.ok(yaml.dump(taxonomy), "text/plain")
        .header("Content-Disposition", "attachment; filename=\"" + taxonomy.getName() + ".yml\"").build();
  }

@PUT
@Operation(summary = "Update taxonomy", description = "Update an existing taxonomy with new vocabulary and term definitions. Can also rename the taxonomy.")
@ApiResponses({
  @ApiResponse(responseCode = "200", description = "Taxonomy successfully updated"),
  @ApiResponse(responseCode = "400", description = "Invalid taxonomy data or name conflict"),
  @ApiResponse(responseCode = "404", description = "Taxonomy not found"),
  @ApiResponse(responseCode = "409", description = "Taxonomy name already exists"),
  @ApiResponse(responseCode = "500", description = "Internal server error")
})
public Response updateTaxonomy(Opal.TaxonomyDto dto) {
    if (!name.equals(dto.getName())) {
      taxonomyService.ensureUniqueTaxonomy(dto.getName());
    }
    taxonomyService.saveTaxonomy(name, Dtos.fromDto(dto));
    return Response.ok().build();
  }

@DELETE
@Operation(summary = "Delete taxonomy", description = "Delete a taxonomy and all its associated vocabularies and terms")
@ApiResponses({
  @ApiResponse(responseCode = "200", description = "Taxonomy successfully deleted"),
  @ApiResponse(responseCode = "404", description = "Taxonomy not found"),
  @ApiResponse(responseCode = "500", description = "Internal server error")
})
public Response deleteTaxonomy() {
    taxonomyService.deleteTaxonomy(name);
    return Response.ok().build();
  }

@GET
@Path("/commits")
@Operation(summary = "Get taxonomy commits", description = "Retrieve the version control history for a taxonomy, showing all commits and changes")
@ApiResponses({
  @ApiResponse(responseCode = "200", description = "Commit history successfully retrieved"),
  @ApiResponse(responseCode = "404", description = "Taxonomy not found"),
  @ApiResponse(responseCode = "500", description = "Internal server error")
})
public Response getCommitsInfo() {
    Iterable<CommitInfo> commitInfos = taxonomyService.getCommitsInfo(name);
    return Response.ok().entity(org.obiba.opal.web.magma.vcs.Dtos.asDto(commitInfos)).build();
  }

@GET
@Path("/commit/{commitId}")
@Operation(summary = "Get commit details", description = "Retrieve detailed information about a specific commit in the taxonomy version history")
@ApiResponses({
  @ApiResponse(responseCode = "200", description = "Commit details successfully retrieved"),
  @ApiResponse(responseCode = "404", description = "Commit or taxonomy not found"),
  @ApiResponse(responseCode = "500", description = "Internal server error")
})
public Response getCommitInfo(@NotNull @PathParam("commitId") String commitId) {
    CommitInfo commitInfo = getVariableDiffInternal(taxonomyService.getCommitInfo(name, commitId), commitId, null);
    return Response.ok().entity(org.obiba.opal.web.magma.vcs.Dtos.asDto(commitInfo)).build();
  }

@GET
@Path("/commit/head/{commitId}")
@Operation(summary = "Get commit diff from HEAD", description = "Retrieve detailed information about a specific commit compared to the current HEAD (latest) version")
@ApiResponses({
  @ApiResponse(responseCode = "200", description = "Commit diff successfully retrieved"),
  @ApiResponse(responseCode = "404", description = "Commit or taxonomy not found"),
  @ApiResponse(responseCode = "500", description = "Internal server error")
})
public Response getCommitInfoFromHead(@NotNull @PathParam("commitId") String commitId) {
    CommitInfo commitInfo = getVariableDiffInternal(taxonomyService.getCommitInfo(name, commitId),
        OpalGitUtils.HEAD_COMMIT_ID, commitId);
    return Response.ok().entity(org.obiba.opal.web.magma.vcs.Dtos.asDto(commitInfo)).build();
  }

@PUT
@Path("/restore/{commitId}")
@Operation(summary = "Restore taxonomy from commit", description = "Restore a taxonomy to a previous state by overwriting the current version with a specific commit")
@ApiResponses({
  @ApiResponse(responseCode = "200", description = "Taxonomy successfully restored from commit"),
  @ApiResponse(responseCode = "404", description = "Commit or taxonomy not found"),
  @ApiResponse(responseCode = "500", description = "Internal server error")
})
public Response restoreCommit(@NotNull @PathParam("commitId") String commitId) {
    String blob = taxonomyService.getBlob(name, commitId);
    taxonomyService.importInputStreamTaxonomy(new ByteArrayInputStream(blob.getBytes(StandardCharsets.UTF_8)), name, true);
    return Response.ok().build();
  }

  @Path("vocabularies")
  public VocabulariesResource getVocabularies() {
    VocabulariesResource resource = applicationContext.getBean(VocabulariesResource.class);
    resource.setTaxonomyName(name);
    return resource;
  }

  @Path("vocabulary/{vocabularyName}")
  public VocabularyResource getVocabulary(@PathParam("vocabularyName") String vocabularyName) {
    VocabularyResource resource = applicationContext.getBean(VocabularyResource.class);
    resource.setTaxonomyName(name);
    resource.setVocabularyName(vocabularyName);
    return resource;
  }

  private CommitInfo getVariableDiffInternal(@NotNull CommitInfo commitInfo, @NotNull String commitId,
      @Nullable String prevCommitId) {
    Iterable<String> diffEntries = taxonomyService.getDiffEntries(name, commitId, prevCommitId);
    return CommitInfo.Builder.createFromObject(commitInfo).diffEntries((List<String>) diffEntries).build();
  }
}
