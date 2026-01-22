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
import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.core.Response;

import org.obiba.opal.web.model.Opal;

public interface VocabulariesResource {

  void setTaxonomyName(String taxonomyName);

  @GET
  @Operation(summary = "Get vocabularies", description = "Retrieve all vocabularies within a taxonomy")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Vocabularies retrieved successfully"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  List<Opal.VocabularyDto> getVocabularies();

  @POST
  @Operation(summary = "Create vocabulary", description = "Create a new vocabulary within a taxonomy")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Vocabulary created successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid vocabulary data"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response createVocabulary(Opal.VocabularyDto vocabulary);
}
