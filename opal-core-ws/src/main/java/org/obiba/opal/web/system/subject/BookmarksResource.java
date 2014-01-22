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

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.obiba.opal.web.model.Opal;

public interface BookmarksResource {

  void setPrincipal(String principal);

  @GET
  List<Opal.BookmarkDto> getBookmarks();

  @POST
  Response addBookmarks(@QueryParam("resource") List<String> resources);

  @DELETE
  Response deleteBookmarks(@QueryParam("resource") List<String> resources);

}
