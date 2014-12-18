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
