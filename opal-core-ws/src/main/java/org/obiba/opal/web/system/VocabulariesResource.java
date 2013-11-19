package org.obiba.opal.web.system;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.core.Response;

import org.obiba.opal.web.model.Opal;

public interface VocabulariesResource {

  void setTaxonomyName(String taxonomyName);

  @GET
  List<Opal.VocabularyDto> getVocabularies();

  @POST
  Response createVocabulary(Opal.VocabularyDto vocabulary);
}
