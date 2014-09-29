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
public class VocabularyResourceImpl implements VocabularyResource {

  private String taxonomyName;

  private String vocabularyName;

  @Autowired
  private TaxonomyService taxonomyService;

  @Override
  public void setTaxonomyName(String taxonomyName) {
    this.taxonomyName = taxonomyName;
  }

  @Override
  public void setVocabularyName(String vocabularyName) {
    this.vocabularyName = vocabularyName;
  }

  @Override
  public Response getVocabulary() {
    Taxonomy taxonomy = taxonomyService.getTaxonomy(taxonomyName);

    if(taxonomy == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    Vocabulary vocabulary = taxonomyService.getVocabulary(taxonomyName, vocabularyName);
    if(vocabulary == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    return Response.ok().entity(Dtos.asDto(vocabulary)).build();
  }

  @Override
  public Response saveVocabulary(Opal.VocabularyDto dto) {
    taxonomyService.saveVocabulary(taxonomyName, Dtos.fromDto(dto));
    return Response.ok().build();
  }
}

