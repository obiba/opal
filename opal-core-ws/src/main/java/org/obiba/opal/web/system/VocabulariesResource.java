/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.system;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.core.Response;

import org.obiba.opal.core.cfg.TaxonomyService;
import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.core.domain.taxonomy.Vocabulary;
import org.obiba.opal.web.taxonomy.Dtos;

import static org.obiba.opal.web.model.Opal.TaxonomyDto.VocabularyDto;

public class VocabulariesResource {

  private final TaxonomyService taxonomyService;

  private final String taxonomyName;

  public VocabulariesResource(TaxonomyService taxonomyService, String taxonomyName) {
    this.taxonomyService = taxonomyService;
    this.taxonomyName = taxonomyName;
  }

  @GET
  public List<VocabularyDto> getVocabularies() {
    List<VocabularyDto> vocabularies = new ArrayList<VocabularyDto>();
    Taxonomy t = taxonomyService.getTaxonomy(taxonomyName);
    if(t != null) {
      for(Vocabulary v : t.getVocabularies()) {
        vocabularies.add(Dtos.asDto(v));
      }
    }
    return vocabularies;
  }

  @POST
  public Response createVocabulary(VocabularyDto vocabulary) {
    Taxonomy tax = taxonomyService.getTaxonomy(taxonomyName);

    if(tax == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    if(tax.hasVocabulary(vocabulary.getName())) {
      return Response.status(Response.Status.CONFLICT).build();
    }

    Vocabulary v = Dtos.fromDto(vocabulary);
    tax.getVocabularies().add(v);

    return Response.ok().build();
  }
}

