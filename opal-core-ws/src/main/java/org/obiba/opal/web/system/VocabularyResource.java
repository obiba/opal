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

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Response;

import org.obiba.opal.core.cfg.NoSuchTaxonomyException;
import org.obiba.opal.core.cfg.NoSuchVocabularyException;
import org.obiba.opal.core.cfg.TaxonomyService;
import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.core.domain.taxonomy.Term;
import org.obiba.opal.core.domain.taxonomy.Vocabulary;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.taxonomy.Dtos;

public class VocabularyResource {

  private final String taxonomyName;

  private final String vocabularyName;

  private final TaxonomyService taxonomyService;

  public VocabularyResource(TaxonomyService taxonomyService, String taxonomyName, String vocabularyName) {
    this.taxonomyService = taxonomyService;
    this.taxonomyName = taxonomyName;
    this.vocabularyName = vocabularyName;
  }

  @SuppressWarnings({ "ConstantConditions", "OverlyLongMethod" })
  @POST
  @Consumes(value = "text/plain")
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

    // Parse csv and add terms
    String[] lines = csv.split("\n");
    Term t = null;
    for(String line : lines) {
      String[] terms = line.split(",");
      int level = terms.length - 1;

      if(level == 0) {
        if(t != null) {
          vocabulary.getTerms().add(t);
        }

        t = new Term(terms[0].replaceAll("\"", ""));
      } else {
        Term parent = t;

        for(int i = 1; i < level; i++) {
          // find parent
          parent = parent.getTerms().get(parent.getTerms().size() - 1);
        }

        // Add new term
        parent.getTerms().add(new Term(terms[terms.length - 1].replaceAll("\"", "")));
      }
    }

//    tax.getVocabularies().add(voc);
    taxonomyService.saveTaxonomy(taxonomy);

    return Response.ok().build();
  }

  @PUT
  public Response saveVocabulary(Opal.VocabularyDto dto) {

    try {

      Vocabulary vocabulary = taxonomyService.getVocabulary(taxonomyName, vocabularyName);
      taxonomyService.saveVocabulary(Dtos.fromDto(dto)); //taxonomyName, vocabularyName, ,vocabulary
    } catch(NoSuchTaxonomyException e) {
      return Response.status(Response.Status.NOT_FOUND).build();
    } catch(NoSuchVocabularyException e) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    return Response.ok().build();
  }
}

