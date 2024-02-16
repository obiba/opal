/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.r;

import com.google.common.collect.Lists;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.r.service.RServerSession;
import org.obiba.opal.web.model.OpalR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope("request")
@Path("/service/r/sessions")
public class RServiceSessionsResource {

  @Autowired
  private OpalRSessionManager opalRSessionManager;

  @GET
  public List<OpalR.RSessionDto> getRSessions() {
    return opalRSessionManager.getRSessions().stream()
        .sorted((rSession1, rSession2) -> {
          Date date1 = rSession1.getTimestamp();
          Date date2 = rSession2.getTimestamp();
          if (date1.equals(date2)) return 0;
          return date1.before(date2) ? 1 : -1;
        })
        .map(Dtos::asDto)
        .collect(Collectors.toList());
  }

  @DELETE
  public Response removeRSessions(@QueryParam("id") List<String> ids) {
    if (ids != null) {
      for (String id : ids) {
        try {
          opalRSessionManager.removeRSession(id);
        } catch (Exception e) {
          // ignore
        }
      }
    }
    return Response.ok().build();
  }
}
