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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.obiba.opal.core.cfg.TaxonomyService;
import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.core.domain.taxonomy.Vocabulary;
import org.obiba.opal.web.magma.Dtos;
import org.obiba.opal.web.model.Opal.TaxonomyDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.obiba.opal.web.model.Opal.TaxonomyDto.VocabularyDto;

@Component
@Path("/system/conf/taxonomy/{name}/vocabularies")
public class VocabulariesResource {

  @PathParam("name")
  private String name;

  private final TaxonomyService taxonomyService;

  @Autowired
  public VocabulariesResource(TaxonomyService taxonomyService) {
    this.taxonomyService = taxonomyService;
  }

  @GET
  public List<TaxonomyDto> getTaxonomies() {
    List<TaxonomyDto> taxonomies = new ArrayList<TaxonomyDto>();
    for(Taxonomy taxonomy : taxonomyService.getTaxonomies()) {
      taxonomies.add(Dtos.asDto(taxonomy));
    }
    return taxonomies;
  }

  @POST
  public Response createVocabulary(VocabularyDto vocabulary) {
    Taxonomy tax = taxonomyService.getTaxonomy(name);

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

