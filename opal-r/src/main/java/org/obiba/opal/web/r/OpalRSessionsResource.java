package org.obiba.opal.web.r;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.web.model.OpalR;
import org.springframework.beans.factory.annotation.Autowired;

public interface OpalRSessionsResource {

  @GET
  List<OpalR.RSessionDto> getRSessionIds();

  @DELETE
  Response removeRSessions();

  @POST
  Response newRSession(@Context UriInfo info);

  @Autowired
  void setOpalRSessionManager(OpalRSessionManager opalRSessionManager);
}
