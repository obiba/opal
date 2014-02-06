/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.system.subject;

import java.util.Objects;

import javax.annotation.Nullable;
import javax.ws.rs.core.Response;

import org.obiba.opal.core.domain.security.Bookmark;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.web.security.Dtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class BookmarkResourceImpl implements BookmarkResource {

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
    this.path = path;
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
    for(Bookmark bookmark : subjectProfileService.getProfile(principal).getBookmarks()) {
      if(Objects.equals(bookmark.getResource(), path)) return bookmark;
    }
    return null;
  }

}
