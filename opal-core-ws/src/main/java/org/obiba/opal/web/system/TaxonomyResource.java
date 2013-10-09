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

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.obiba.opal.core.cfg.TaxonomyService;
import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.taxonomy.Dtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/system/conf/taxonomy/{name}")
public class TaxonomyResource {

  @Autowired
  private TaxonomyService taxonomyService;

  @PathParam("name")
  private String name;

  @GET
  public Opal.TaxonomyDto getTaxonomy() {
    Taxonomy taxonomy = taxonomyService.getTaxonomy(name);
    return Dtos.asDto(taxonomy);
  }

  @PUT
  public Response updateTaxonomy(Opal.TaxonomyDto dto) {
    taxonomyService.saveTaxonomy(Dtos.fromDto(dto));
    return Response.ok().build();
  }

  @Path("vocabularies")
  public VocabulariesResource getVocabularies() {
    return new VocabulariesResource(taxonomyService, name);
  }

  @Path("vocabulary/{vocabularyName}")
  public VocabularyResource getVocabulary(@PathParam("vocabularyName") String vocabularyName) {
    return new VocabularyResource(taxonomyService, name, vocabularyName);
  }
}
