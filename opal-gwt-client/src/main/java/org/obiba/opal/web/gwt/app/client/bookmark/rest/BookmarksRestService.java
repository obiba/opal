/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.bookmark.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.obiba.opal.web.model.client.opal.BookmarkDto;

import com.gwtplatform.dispatch.rest.shared.RestAction;
import com.gwtplatform.dispatch.rest.shared.RestService;

@Path("/system/subject-profile/_current/bookmarks")
public interface BookmarksRestService extends RestService {

  @GET
  RestAction<List<BookmarkDto>> getBookmarks();

  @POST
  RestAction<Void> addBookmarks(@QueryParam("resource") List<String> resources);

}
