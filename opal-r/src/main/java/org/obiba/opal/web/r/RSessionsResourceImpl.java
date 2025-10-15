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

import com.google.common.base.Strings;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.PathSegment;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.obiba.opal.r.service.*;
import org.obiba.opal.r.service.tasks.RSessionStateWaiter;
import org.obiba.opal.web.model.OpalR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Handles the list and the creation of the R sessions of the invoking Opal user.
 */
@Component("opalRSessionsResource")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class RSessionsResourceImpl implements RSessionsResource {

  static final String R_CONTEXT = "R";

  @Autowired
  private RServerManagerService rServerManagerService;

  @Value("${org.obiba.opal.r.endpoint}")
  private boolean plainREnabled;

  protected OpalRSessionManager opalRSessionManager;

  @Autowired
  @Override
  public void setOpalRSessionManager(OpalRSessionManager opalRSessionManager) {
    this.opalRSessionManager = opalRSessionManager;
  }

  @Override
  public List<OpalR.RSessionDto> getRSessions() {
    return opalRSessionManager.getSubjectRSessions().stream()
        .map(Dtos::asDto)
        .toList();
  }

  @Override
  public Response removeRSessions() {
    opalRSessionManager.removeSubjectRSessions();
    return Response.ok().build();
  }

  @Override
  public Response newRSession(UriInfo info, String restore, String profile, boolean wait) {
    if (!createRSessionEnabled())
      throw new ForbiddenException("Plain R service endpoint is not enabled");
    RServerSession rSession = opalRSessionManager.newSubjectRSession(createProfile(profile), withInitiator());
    onNewRSession(rSession);
    if (wait || !Strings.isNullOrEmpty(restore)) {
      RSessionStateWaiter waiter = new RSessionStateWaiter(rSession, restore) {
        @Override
        protected void onRunning(RServerSession rSession, String restore) {
           if (!Strings.isNullOrEmpty(restore)) {
             opalRSessionManager.restoreSubjectRSession(rSession.getId(), restore);
           }
        }
      };
      if (wait) {
        waiter.run();
      } else {
        CompletableFuture.runAsync(waiter);
      }
    }
    URI location = getLocation(info, rSession.getId());
    return Response.created(location).entity(Dtos.asDto(rSession))
        .build();
  }

  @Override
  public Response testNewRSession(String profile) {
    try {
      RServerSession rSession = opalRSessionManager.newSubjectRSession(createProfile(profile));
      opalRSessionManager.removeRSession(rSession.getId());
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    return Response.ok().build();
  }

  protected RServerProfile createProfile(String profileName) {
    if (Strings.isNullOrEmpty(profileName))
      return rServerManagerService.getDefaultRServerProfile();
    return new DefaultRServerProfile(profileName);
  }

  protected boolean createRSessionEnabled() {
    return plainREnabled;
  }

  protected void onNewRSession(RServerSession rSession) {
    rSession.setExecutionContext(R_CONTEXT);
  }

  protected RContextInitiator withInitiator() {
    return null;
  }

  URI getLocation(UriInfo info, String id) {
    List<PathSegment> segments = info.getPathSegments();
    List<PathSegment> patate = segments.subList(0, segments.size() - 1);
    StringBuilder root = new StringBuilder();
    for (PathSegment s : patate) {
      root.append('/').append(s.getPath());
    }
    root.append("/session");

    return info.getBaseUriBuilder().path(root.toString()).path(id).build();
  }

  public static class DefaultRServerProfile implements RServerProfile {

    private final String name;

    public DefaultRServerProfile(String name) {
      this.name = name;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public String getCluster() {
      return name;
    }
  }
}
