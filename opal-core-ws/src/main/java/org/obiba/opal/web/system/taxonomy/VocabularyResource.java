/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.system.taxonomy;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

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
