/*
 * Copyright (c) 2015 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.r;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.obiba.opal.r.service.OpalRSession;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.web.model.OpalR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
@Scope("request")
@Path("/service/r/sessions")
public class RServiceSessionsResource {

  @Autowired
  private OpalRSessionManager opalRSessionManager;

  @GET
  public List<OpalR.RSessionDto> getRSessionIds() {
    List<OpalR.RSessionDto> rSessions = Lists.newArrayList();
    for(OpalRSession rSession : opalRSessionManager.getRSessions()) {
      rSessions.add(Dtos.asDto(rSession));
    }
    return rSessions;
  }

}
