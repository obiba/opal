/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

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

import javax.annotation.Nullable;

@Component
@Scope("request")
@Path("/system/conf/taxonomy/{name}")
public class TaxonomyResource {

  @Autowired
  private TaxonomyService taxonomyService;

  @Autowired
  private ApplicationContext applicationContext;

  @PathParam("name")
  private String name;

  @GET
  @NoAuthorization
  public Opal.TaxonomyDto getTaxonomy() {
    Taxonomy taxonomy = taxonomyService.getTaxonomy(name);
    if(taxonomy == null) throw new NoSuchTaxonomyException(name);
    return Dtos.asDto(taxonomy);
  }

  @GET
  @Produces(value = "text/plain")
  @Path("_download")
  public Response download() {
    Taxonomy taxonomy = taxonomyService.getTaxonomy(name);
    if(taxonomy == null) throw new NoSuchTaxonomyException(name);
    TaxonomyYaml yaml = new TaxonomyYaml();
    return Response.ok(yaml.dump(taxonomy), "text/plain")
        .header("Content-Disposition", "attachment; filename=\"" + taxonomy.getName() + ".yml\"").build();
  }

  @PUT
  public Response updateTaxonomy(Opal.TaxonomyDto dto) {
    if (!name.equals(dto.getName()) && taxonomyService.hasTaxonomy(dto.getName()))
      return Response.status(Response.Status.BAD_REQUEST).build();
    taxonomyService.saveTaxonomy(name, Dtos.fromDto(dto));
    return Response.ok().build();
  }

  @DELETE
  public Response deleteTaxonomy() {
    taxonomyService.deleteTaxonomy(name);
    return Response.ok().build();
  }

  @GET
  @Path("/commits")
  public Response getCommitsInfo() {
    Iterable<CommitInfo> commitInfos = taxonomyService.getCommitsInfo(name);
    return Response.ok().entity(org.obiba.opal.web.magma.vcs.Dtos.asDto(commitInfos)).build();
  }

  @GET
  @Path("/commit/{commitId}")
  public Response getCommitInfo(@NotNull @PathParam("commitId") String commitId) {
    CommitInfo commitInfo = getVariableDiffInternal(taxonomyService.getCommitInfo(name, commitId), commitId, null);
    return Response.ok().entity(org.obiba.opal.web.magma.vcs.Dtos.asDto(commitInfo)).build();
  }

  @GET
  @Path("/commit/head/{commitId}")
  public Response getCommitInfoFromHead(@NotNull @PathParam("commitId") String commitId) {
    CommitInfo commitInfo = getVariableDiffInternal(taxonomyService.getCommitInfo(name, commitId),
        OpalGitUtils.HEAD_COMMIT_ID, commitId);
    return Response.ok().entity(org.obiba.opal.web.magma.vcs.Dtos.asDto(commitInfo)).build();
  }

  @PUT
  @Path("/restore/{commitId}")
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
