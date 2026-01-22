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
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.core.Response;

public interface BookmarkResource {

  void setPrincipal(String principal);

  void setPath(String path);

  @GET
  @Operation(summary = "Get bookmark", description = "Retrieve a specific bookmark for the user")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Bookmark retrieved successfully"),
    @ApiResponse(responseCode = "404", description = "Bookmark not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response get();

  @DELETE
  @Operation(summary = "Delete bookmark", description = "Delete a specific bookmark")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Bookmark deleted successfully"),
    @ApiResponse(responseCode = "404", description = "Bookmark not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response delete();
}
