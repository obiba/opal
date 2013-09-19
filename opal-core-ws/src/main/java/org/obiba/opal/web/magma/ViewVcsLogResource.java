package org.obiba.opal.web.magma;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.obiba.opal.core.vcs.VersionControlSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/datasource/{datasourceName}/view/{viewName}/vcs")
public class ViewVcsLogResource {

  @Autowired
  private VersionControlSystem vcs;

  @PathParam("datasourceName")
  private String datasource;

  @PathParam("viewName")
  private String view;

  @GET
  @Path("/commits")
  public String getCommitsInfo() {
    return vcs.getViewCommitsInfo(datasource, view).toString();
  }


}
