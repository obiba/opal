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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.ws.rs.core.Response;

import org.obiba.opal.core.domain.security.Bookmark;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.security.Dtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class BookmarksResourceImpl implements BookmarksResource {

  private String principal;

  @Autowired
  private SubjectProfileService subjectProfileService;

  @Override
  public void setPrincipal(String principal) {
    this.principal = principal;
  }

  @Override
  public List<Opal.BookmarkDto> getBookmarks() {

    List<Bookmark> bookmarks = new ArrayList<>(subjectProfileService.getProfile(principal).getBookmarks());
    Collections.sort(bookmarks, Bookmark.RESOURCE_COMPARATOR);

    List<Opal.BookmarkDto> dtos = new ArrayList<>();
    for(Bookmark bookmark : bookmarks) {
      dtos.add(Dtos.asDto(bookmark));
    }
    return dtos;
  }

  @Override
  public Response addBookmarks(List<String> resources) {
    subjectProfileService.addBookmarks(principal, resources);
    return Response.ok().build();
  }

}
