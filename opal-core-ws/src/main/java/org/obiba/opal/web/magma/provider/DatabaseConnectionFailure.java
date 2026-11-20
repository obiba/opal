/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.magma.provider;

import com.mongodb.MongoSocketException;
import com.mongodb.MongoSocketReadTimeoutException;
import com.mongodb.MongoTimeoutException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;
import org.obiba.opal.core.repository.ProjectRepository;
import org.obiba.opal.core.service.ProjectsState;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.sql.SQLException;

import static jakarta.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;

/**
 * A request that failed because the database of a project could not be reached is answered with a 503 rather than a
 * 500, and the project is put in error, so that its datasource gets loaded again once the database is back.
 */
@Component
public class DatabaseConnectionFailure {

  private static final Logger log = LoggerFactory.getLogger(DatabaseConnectionFailure.class);

  private final ProjectsState projectsState;

  private final ProjectRepository projectRepository;

  @Autowired
  public DatabaseConnectionFailure(ProjectsState projectsState, ProjectRepository projectRepository) {
    this.projectsState = projectsState;
    this.projectRepository = projectRepository;
  }

  static boolean isConnectionFailure(Throwable exception) {
    for (Throwable cause = exception; cause != null; cause = cause.getCause() == cause ? null : cause.getCause()) {
      // SQL state class 08 is "connection exception", whatever the server
      if (cause instanceof SQLException sql && sql.getSQLState() != null && sql.getSQLState().startsWith("08")) return true;
      // a read timeout is a slow query, not a lost database
      if (cause instanceof MongoSocketReadTimeoutException) return false;
      if (cause instanceof MongoSocketException || cause instanceof MongoTimeoutException) return true;
    }
    return false;
  }

  Response toResponse(Throwable exception) {
    String project = getRequestedProject();
    try {
      // logged once, not on each request while the database is down
      if (project != null && projectRepository.findByName(project).isPresent()
          && projectsState.updateProjectState(project, ProjectsState.State.READY, ProjectsState.State.ERRORS)) {
        log.warn("Database of project {} could not be reached: {}", project, exception.getMessage());
      } else {
        log.debug("Database could not be reached: {}", exception.getMessage());
      }
    } catch (RuntimeException e) {
      // the configuration database may be the one that is down
      log.warn("Database could not be reached: {}", exception.getMessage());
    }
    return Response.status(SERVICE_UNAVAILABLE).type("application/x-protobuf+json")
        .entity(ClientErrorDtos.getErrorMessage(SERVICE_UNAVAILABLE, "DatabaseConnectionFailed").build()).build();
  }

  /**
   * The project's data are requested at /datasource/{name}/... or /project/{name}/...
   */
  private static String getRequestedProject() {
    if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) return null;
    HttpServletRequest request = attributes.getRequest();
    String path = request.getPathInfo();
    if (path == null) return null;
    String[] segments = path.split("/");
    // path starts with a slash, so the first segment is empty
    if (segments.length > 2 && ("datasource".equals(segments[1]) || "project".equals(segments[1]))) return segments[2];
    return null;
  }

}
