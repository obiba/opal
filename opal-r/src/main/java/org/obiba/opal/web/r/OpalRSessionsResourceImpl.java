/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
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

import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.obiba.opal.r.service.OpalRSession;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.web.model.OpalR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

/**
 * Handles the list and the creation of the R sessions of the invoking Opal user.
 */
@Component
@Transactional
public class OpalRSessionsResourceImpl implements OpalRSessionsResource {

  private OpalRSessionManager opalRSessionManager;

  @Autowired
  @Override
  public void setOpalRSessionManager(OpalRSessionManager opalRSessionManager) {
    this.opalRSessionManager = opalRSessionManager;
  }

  @Override
  public List<OpalR.RSessionDto> getRSessionIds() {
    List<OpalR.RSessionDto> rSessions = Lists.newArrayList();
    for(OpalRSession rSession : opalRSessionManager.getSubjectRSessions()) {
      rSessions.add(Dtos.asDto(rSession));
    }
    return rSessions;
  }

  @Override
  public Response removeRSessions() {
    opalRSessionManager.removeSubjectRSessions();
    return Response.ok().build();
  }

  @Override
  public Response newRSession(UriInfo info) {
    OpalRSession rSession = opalRSessionManager.newSubjectRSession();
    URI location = getLocation(info, rSession.getId());
    return Response.created(location).entity(Dtos.asDto(rSession))
        .build();
  }

  URI getLocation(UriInfo info, String id) {
    List<PathSegment> segments = info.getPathSegments();
    List<PathSegment> patate = segments.subList(0, segments.size() - 1);
    StringBuilder root = new StringBuilder();
    for(PathSegment s : patate) {
      root.append('/').append(s.getPath());
    }
    root.append("/session");

    return info.getBaseUriBuilder().path(root.toString()).path(id).build();
  }
}
