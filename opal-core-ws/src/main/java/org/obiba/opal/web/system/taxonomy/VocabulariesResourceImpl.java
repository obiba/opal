/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.system.taxonomy;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.obiba.opal.core.cfg.TaxonomyService;
import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.core.domain.taxonomy.Vocabulary;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.taxonomy.Dtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class VocabulariesResourceImpl implements VocabulariesResource {

  private String taxonomyName;

  @Autowired
  private TaxonomyService taxonomyService;

  @Override
  public void setTaxonomyName(String taxonomyName) {
    this.taxonomyName = taxonomyName;
  }

  @Override
  public List<Opal.VocabularyDto> getVocabularies() {
    List<Opal.VocabularyDto> vocabularies = new ArrayList<>();

    for(Vocabulary v : taxonomyService.getVocabularies(taxonomyName)) {
      vocabularies.add(Dtos.asDto(v));
    }

    return vocabularies;
  }

  @Override
  public Response createVocabulary(Opal.VocabularyDto vocabulary) {
    Taxonomy tax = taxonomyService.getTaxonomy(taxonomyName);

    if(tax == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    if(tax.hasVocabulary(vocabulary.getName())) {
      return Response.status(Response.Status.CONFLICT).build();
    }

    taxonomyService.saveVocabulary(taxonomyName, Dtos.fromDto(vocabulary));

    return Response.ok().build();
  }
}

