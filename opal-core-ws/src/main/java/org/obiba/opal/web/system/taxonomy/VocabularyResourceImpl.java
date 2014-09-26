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

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.ws.rs.core.Response;

import org.obiba.opal.core.cfg.NoSuchTaxonomyException;
import org.obiba.opal.core.cfg.NoSuchVocabularyException;
import org.obiba.opal.core.cfg.TaxonomyService;
import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.core.domain.taxonomy.Term;
import org.obiba.opal.core.domain.taxonomy.Vocabulary;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.taxonomy.Dtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;

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
  public Response addVocabularyTerms(String csv) {
    Taxonomy taxonomy = taxonomyService.getTaxonomy(taxonomyName);

    if(taxonomy == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    Vocabulary vocabulary = taxonomyService.getVocabulary(taxonomyName, vocabularyName);
    if(vocabulary == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    vocabulary.getTerms().clear();

    try {
      parseStringAsCsv(csv, vocabulary);
    } catch(IOException e) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }

    taxonomyService.saveVocabulary(null, vocabulary);

    return Response.ok().build();
  }

  private void parseStringAsCsv(String csv, Vocabulary vocabulary) throws IOException {// Parse csv and add terms
    CSVReader reader = new CSVReader(new StringReader(csv));
    List<String[]> lines = reader.readAll();

    Term t = null;
    for(String[] terms : lines) {

      int level = terms.length - 1;
      if(level == 0) {
        if(t != null) {
          vocabulary.getTerms().add(t);
        }

        t = new Term(terms[0]);
      } else {
        Term parent = t;
      }
    }
  }

  @Override
  public Response saveVocabulary(Opal.VocabularyDto dto) {
    try {
      taxonomyService.saveVocabulary(taxonomyName, Dtos.fromDto(dto));
    } catch(NoSuchTaxonomyException e) {
      return Response.status(Response.Status.NOT_FOUND).build();
    } catch(NoSuchVocabularyException e) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    return Response.ok().build();
  }
}

