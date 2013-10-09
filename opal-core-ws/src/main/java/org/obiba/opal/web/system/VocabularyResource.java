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
import javax.ws.rs.core.Response;

import org.obiba.opal.core.cfg.TaxonomyService;
import org.obiba.opal.core.domain.taxonomy.HasTerms;
import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.core.domain.taxonomy.Term;
import org.obiba.opal.core.domain.taxonomy.Vocabulary;

public class VocabularyResource {

  private final String taxonomyName;

  private final String vocabularyName;

  private final TaxonomyService taxonomyService;

  public VocabularyResource(TaxonomyService taxonomyService, String taxonomyName, String vocabularyName) {
    this.taxonomyService = taxonomyService;
    this.taxonomyName = taxonomyName;
    this.vocabularyName = vocabularyName;
  }

  @SuppressWarnings("ConstantConditions")
  @POST
  @Consumes(value = "text/plain")
  public Response addVocabularyTerms(String input) {
    Taxonomy tax = taxonomyService.getTaxonomy(taxonomyName);

    if(tax == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    Vocabulary voc = tax.getVocabulary(vocabularyName);
    if(voc == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    voc.getTerms().clear();

    // Parse input and add terms
    String[] lines = input.split("\n");
    Term t = null;
    for(String line : lines) {
      String[] terms = line.split(",");
      int level = terms.length - 1;

      if(level == 0) {
        if(t != null) {
          voc.add(t);
        }

        t = new Term(terms[0].replaceAll("\"", ""));
      } else {
        HasTerms parent = t;

        for(int i = 1; i < level; i++) {
          // find parent
          parent = parent.getTerms().get(parent.getTerms().size() - 1);
        }

        // Add new term
        parent.add(new Term(terms[terms.length - 1].replaceAll("\"", "")));
      }
    }

    tax.add(voc);
    taxonomyService.addOrReplaceTaxonomy(tax);

    return Response.ok().build();
  }
}

