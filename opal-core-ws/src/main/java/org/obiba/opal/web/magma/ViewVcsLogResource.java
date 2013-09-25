package org.obiba.opal.web.magma;

import java.util.List;

import javax.annotation.Nonnull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.obiba.opal.core.vcs.CommitInfo;
import org.obiba.opal.core.vcs.OpalVersionControlSystem;
import org.obiba.opal.core.vcs.support.OpalGitUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/datasource/{datasourceName}/view/{viewName}/vcs")
public class ViewVcsLogResource {

  @Autowired
  private OpalVersionControlSystem vcs;

  @PathParam("datasourceName")
  private String datasource;

  @PathParam("viewName")
  private String view;

  @GET
  @Path("/commits")
  public String getCommitsInfo() {
    return vcs.getCommitsInfo(datasource, OpalGitUtils.getViewFilePath(view)).toString();
  }

  @GET
  @Path("/variable/{variableName}/commits")
  public String getVariableCommitsInfo(@Nonnull @PathParam("variableName") String variabeName) {
    return vcs.getCommitsInfo(datasource, OpalGitUtils.getVariableFilePath(view, variabeName)).toString();
  }

  @GET
  @Path("/variable/{variableName}/commit/{commitId}")
  public CommitInfo getVariableCommitInfo(@Nonnull @PathParam("variableName") String variabeName,
      @Nonnull @PathParam("commitId") String commitId) {

    String path = OpalGitUtils.getVariableFilePath(view, variabeName);
    CommitInfo commitInfo = vcs.getCommitInfo(datasource, path, commitId);
    List<String> diffEntries = vcs.getDiffEntries(datasource, commitId, path);

    return CommitInfo.Builder.createFromObject(commitInfo).setDiffEntries(diffEntries).build();
  }

  @GET
  @Path("/variable/{variableName}/blob/{commitId}")
  public String getVariableContent(@Nonnull @PathParam("variableName") String variabeName,
      @Nonnull @PathParam("commitId") String commitId) {
    return vcs.getBlob(datasource, OpalGitUtils.getVariableFilePath(view, variabeName), commitId).toString();
  }


}
