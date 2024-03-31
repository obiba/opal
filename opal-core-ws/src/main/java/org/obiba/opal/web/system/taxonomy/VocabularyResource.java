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
  Response getVocabulary();

  @PUT
  Response saveVocabulary(Opal.VocabularyDto dto);

  @DELETE
  Response deleteVocabulary();

  @POST
  @Path("terms")
  Response createTerm(Opal.TermDto dto);

  @PUT
  @Path("term/{term}")
  Response saveTerm(@PathParam("term") String term, Opal.TermDto dto);

  @DELETE
  @Path("term/{term}")
  Response deleteTerm(@PathParam("term") String term);
}
