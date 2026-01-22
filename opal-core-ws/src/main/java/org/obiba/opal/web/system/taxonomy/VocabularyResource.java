/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.system.taxonomy;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import org.obiba.opal.web.model.Opal;

public interface VocabularyResource {

  void setTaxonomyName(String taxonomyName);

  void setVocabularyName(String vocabularyName);

  @GET
  @Operation(summary = "Get vocabulary", description = "Retrieve a specific vocabulary from a taxonomy")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Vocabulary retrieved successfully"),
    @ApiResponse(responseCode = "404", description = "Vocabulary not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response getVocabulary();

  @PUT
  @Operation(summary = "Save vocabulary", description = "Save/update a vocabulary in a taxonomy")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Vocabulary saved successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid vocabulary data"),
    @ApiResponse(responseCode = "404", description = "Vocabulary not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response saveVocabulary(Opal.VocabularyDto dto);

  @DELETE
  @Operation(summary = "Delete vocabulary", description = "Delete a vocabulary from a taxonomy")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Vocabulary deleted successfully"),
    @ApiResponse(responseCode = "404", description = "Vocabulary not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response deleteVocabulary();

  @POST
  @Path("terms")
  @Operation(summary = "Create term", description = "Create a new term within a vocabulary")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Term created successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid term data"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response createTerm(Opal.TermDto dto);

  @PUT
  @Path("term/{term}")
  @Operation(summary = "Save term", description = "Save/update a term within a vocabulary")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Term saved successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid term data"),
    @ApiResponse(responseCode = "404", description = "Term not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response saveTerm(@PathParam("term") String term, Opal.TermDto dto);

  @DELETE
  @Path("term/{term}")
  @Operation(summary = "Delete term", description = "Delete a term from a vocabulary")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Term deleted successfully"),
    @ApiResponse(responseCode = "404", description = "Term not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response deleteTerm(@PathParam("term") String term);
}
