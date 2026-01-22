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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import org.obiba.opal.web.model.Opal;

public interface BookmarksResource {

  void setPrincipal(String principal);

  @GET
  @Operation(summary = "Get bookmarks", description = "Retrieve all bookmarks for the user")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Bookmarks retrieved successfully"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  List<Opal.BookmarkDto> getBookmarks();

  @POST
  @Operation(summary = "Add bookmarks", description = "Add multiple bookmarks for the user")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Bookmarks added successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid resource URLs"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response addBookmarks(@QueryParam("resource") List<String> resources);

}
