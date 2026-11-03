/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.magma.vcs;

import java.util.List;

import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import org.obiba.git.CommitInfo;
import org.obiba.git.TagInfo;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.ValueView;
import org.obiba.magma.views.ViewAwareDatasource;
import org.obiba.magma.views.ViewManager;
import org.obiba.opal.core.cfg.OpalViewPersistenceStrategy;
import org.obiba.opal.core.vcs.OpalGitUtils;
import org.obiba.opal.core.vcs.OpalVersionControlSystem;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.magma.ViewResourceImpl;
import org.obiba.opal.web.magma.view.ViewDtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;
import jakarta.annotation.Nullable;

@Component
@Transactional
@Scope("request")
@Path("/datasource/{datasourceName}/view/{viewName}/vcs")
@Tag(name = "Datasources", description = "Operations on datasources")
public class ViewVcsLogResource {

  @Autowired
  private OpalVersionControlSystem vcs;

  @Autowired
  private OpalViewPersistenceStrategy viewPersistenceStrategy;

  @Autowired
  private ViewManager viewManager;

  @Autowired
  private ViewDtos viewDtos;

  @PathParam("datasourceName")
  private String datasource;

  @PathParam("viewName")
  private String view;

  /**
   * History of the view folder: the view, the entity filter and the variable scripts.
   */
  @GET
  @Path("/commits")
  public Response getCommitsInfo() {
    Iterable<CommitInfo> commitInfos = vcs.getCommitsInfo(datasource, view, view);
    return Response.ok().entity(Dtos.asDto(commitInfos)).build();
  }

  @GET
  @Path("/commit/{commitId}")
  public Response getCommitInfo(@NotNull @PathParam("commitId") String commitId) {
    return Response.ok().entity(
        Dtos.asDto(getVariableDiffInternal(vcs.getCommitInfo(datasource, view, commitId), view, commitId, null)))
        .build();
  }

  @GET
  @Path("/commit/head/{commitId}")
  public Response getCommitInfoFromHead(@NotNull @PathParam("commitId") String commitId) {
    return Response.ok().entity(Dtos.asDto(
        getVariableDiffInternal(vcs.getCommitInfo(datasource, view, commitId), view, OpalGitUtils.HEAD_COMMIT_ID,
            commitId))).build();
  }

  /**
   * Replace the view by its version at the commit. The view save adds a new commit, the history is kept.
   */
  @POST
  @Path("/commit/{commitId}/_restore")
  public Response restore(@NotNull @PathParam("commitId") String commitId) {
    // the view itself, not the table of the secured datasource: its datasource is the view aware one
    ValueView current = viewManager.getView(datasource, view);
    if(!(current.getDatasource() instanceof ViewAwareDatasource viewAwareDatasource))
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(ClientErrorDtos.getErrorMessage(Response.Status.BAD_REQUEST, "ViewRestoreFailed", view).build())
          .build();
    ValueView restored = viewPersistenceStrategy.readView(datasource, view, commitId);
    // the deserialized view has no datasource until it is added, and the view dto needs it
    restored.setDatasource(viewAwareDatasource);
    try {
      Initialisables.initialise(restored);
    } catch(RuntimeException e) {
      // for instance a source table does not exist anymore
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(ClientErrorDtos.getErrorMessage(Response.Status.BAD_REQUEST, "ViewRestoreFailed", e)).build();
    }
    if(!ViewResourceImpl.checkValuesPermissions(current, viewDtos.asDto(restored)))
      return Response.status(Response.Status.FORBIDDEN).build();
    viewManager.addView(datasource, restored, "Restore view from " + getCommitLabel(commitId), null);
    return Response.ok().build();
  }

  @GET
  @Path("/tags")
  public Response getTags() {
    return Response.ok().entity(Dtos.asTagsDto(vcs.getTags(datasource, view))).build();
  }

  /**
   * Tag the latest version of the view. An existing or invalid tag name is rejected by git.
   * Not 201: the created resource permissions would outlive the view edit permission.
   */
  @POST
  @Path("/tags")
  public Response createTag(@QueryParam("name") String name, @QueryParam("message") String message) {
    if(Strings.isNullOrEmpty(name) || name.isBlank())
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(ClientErrorDtos.getErrorMessage(Response.Status.BAD_REQUEST, "TagNameRequired").build()).build();
    vcs.createTag(datasource, view, name, message);
    return Response.ok().build();
  }

  @GET
  @Path("/variable/{variableName}/commits")
  public Response getVariableCommitsInfo(@NotNull @PathParam("variableName") String variableName) {
    Iterable<CommitInfo> commitInfos = vcs
        .getCommitsInfo(datasource, view, OpalGitUtils.getVariableFilePath(view, variableName));
    return Response.ok().entity(Dtos.asDto(commitInfos)).build();
  }

  @GET
  @Path("/variable/{variableName}/commit/{commitId}")
  public Response getVariableCommitInfo(@NotNull @PathParam("variableName") String variableName,
      @NotNull @PathParam("commitId") String commitId) {

    String path = OpalGitUtils.getVariableFilePath(view, variableName);
    return Response.ok().entity(
        Dtos.asDto(getVariableDiffInternal(vcs.getCommitInfo(datasource, path, commitId), path, commitId, null)))
        .build();
  }

  @GET
  @Path("/variable/{variableName}/commit/head/{commitId}")
  public Response getVariableCommitInfoFromHead(@NotNull @PathParam("variableName") String variableName,
      @NotNull @PathParam("commitId") String commitId) {

    String path = OpalGitUtils.getVariableFilePath(view, variableName);
    return Response.ok().entity(Dtos.asDto(
        getVariableDiffInternal(vcs.getCommitInfo(datasource, path, commitId), path, OpalGitUtils.HEAD_COMMIT_ID,
            commitId))).build();
  }

  @GET
  @Path("/variable/{variableName}/blob/{commitId}")
  public Response getVariableContent(@NotNull @PathParam("variableName") String variableName,
      @NotNull @PathParam("commitId") String commitId) {
    String blob = vcs.getBlob(datasource, OpalGitUtils.getVariableFilePath(view, variableName), commitId);
    String path = OpalGitUtils.getVariableFilePath(view, variableName);
    CommitInfo info = vcs.getCommitInfo(datasource, path, commitId);
    return Response.ok().entity(Dtos.asDto(CommitInfo.Builder.createFromObject(info).blob(blob).build())).build();
  }

  private CommitInfo getVariableDiffInternal(@NotNull CommitInfo commitInfo, @NotNull String path,
      @NotNull String commitId, @Nullable String prevCommitId) {
    Iterable<String> diffEntries = vcs.getDiffEntries(datasource, commitId, prevCommitId, path);
    return CommitInfo.Builder.createFromObject(commitInfo).diffEntries((List<String>) diffEntries).build();
  }

  private String getCommitLabel(String commitId) {
    for(TagInfo tag : vcs.getTags(datasource, view)) {
      if(tag.getCommitId().equals(commitId)) return tag.getName();
    }
    return commitId.substring(0, Math.min(7, commitId.length()));
  }
}
