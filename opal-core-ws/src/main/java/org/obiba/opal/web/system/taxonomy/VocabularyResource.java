package org.obiba.opal.web.system.taxonomy;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Response;

import org.obiba.opal.web.model.Opal;

public interface VocabularyResource {

  void setTaxonomyName(String taxonomyName);

  void setVocabularyName(String vocabularyName);

  @GET
  Response getVocabulary();

  @PUT
  Response saveVocabulary(Opal.VocabularyDto dto);
}
