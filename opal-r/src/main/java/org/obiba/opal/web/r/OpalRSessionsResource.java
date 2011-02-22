/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.r;

import java.net.URI;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.obiba.opal.r.service.OpalRSession;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.web.model.OpalR;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Handles the list and the creation of the R sessions of the invoking Opal user.
 */
public class OpalRSessionsResource {

  private final OpalRSessionManager opalRSessionManager;

  public OpalRSessionsResource(OpalRSessionManager opalRSessionManager) {
    if(opalRSessionManager == null) throw new IllegalArgumentException("opalRSessionManager cannot be null");
    this.opalRSessionManager = opalRSessionManager;
  }

  @GET
  public List<OpalR.RSessionDto> getRSessionIds() {
    final List<OpalR.RSessionDto> rSessions = Lists.newArrayList();
    for(OpalRSession rSession : opalRSessionManager.getSubjectRSessions()) {
      rSessions.add(Dtos.asDto(rSession));
    }
    return rSessions;
  }

  @POST
  public Response newCurrentRSession(@Context UriInfo info) {
    OpalRSession rSession = opalRSessionManager.newSubjectCurrentRSession();
    List<URI> locations = getLocations(info, rSession.getId());
    return Response.created(locations.get(0)).header("X-Alt-Location", locations.get(1)).entity(Dtos.asDto(rSession)).build();
  }

  protected List<URI> getLocations(UriInfo info, String id) {
    List<PathSegment> segments = info.getPathSegments();
    List<PathSegment> patate = segments.subList(0, segments.size() - 1);
    StringBuilder root = new StringBuilder();
    for(PathSegment s : patate) {
      root.append('/').append(s.getPath());
    }
    root.append("/session");

    return ImmutableList.of(info.getBaseUriBuilder().path(root.toString()).path(id).build(), info.getBaseUriBuilder().path(root.toString()).path("current").build());
  }
}
