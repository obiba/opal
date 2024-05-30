/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.system.subject;

import java.util.Objects;

import jakarta.annotation.Nullable;
import jakarta.ws.rs.core.Response;

import org.obiba.opal.core.domain.security.Bookmark;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.web.security.Dtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class BookmarkResourceImpl implements BookmarkResource {

  private static final Logger log = LoggerFactory.getLogger(BookmarkResourceImpl.class);

  private String principal;

  private String path;

  @Autowired
  private SubjectProfileService subjectProfileService;

  @Override
  public void setPrincipal(String principal) {
    this.principal = principal;
  }

  @Override
  public void setPath(String path) {
    this.path = path.startsWith("/") ? path : "/" + path;
  }

  @Override
  public Response get() {
    Bookmark bookmark = getBookmark();
    return bookmark == null
        ? Response.status(Response.Status.NOT_FOUND).build()
        : Response.ok().entity(Dtos.asDto(bookmark)).build();
  }

  @Override
  public Response delete() {
    subjectProfileService.deleteBookmark(principal, path);
    return Response.ok().build();
  }

  @Nullable
  private Bookmark getBookmark() {
    log.debug("Searching for bookmark with path: {} for principal: {}", path, principal);
    for(Bookmark bookmark : subjectProfileService.getProfile(principal).getBookmarks()) {
      if(Objects.equals(bookmark.getResource(), path)) {
        log.debug("Found bookmark: {} matching path: {}", bookmark, path);
        return bookmark;
      }
    }
    log.debug("{} has no bookmarks matching path: {}", principal, path);
    return null;
  }

}
