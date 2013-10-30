/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;

import org.obiba.opal.core.cfg.TaxonomyService;
import org.obiba.opal.core.domain.HasUniqueProperties;
import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.core.domain.taxonomy.Vocabulary;
import org.obiba.opal.core.service.OrientDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;

@Component
public class TaxonomyServiceImpl implements TaxonomyService {

  @Autowired
  private OrientDbService orientDbService;

  @Override
  @PostConstruct
  public void start() {
    orientDbService.createUniqueIndex(Taxonomy.class);
    orientDbService.createUniqueIndex(Vocabulary.class);
  }

  @Override
  public void stop() {
  }

  @Override
  public Iterable<Taxonomy> getTaxonomies() {
    return orientDbService.list(Taxonomy.class);
  }

  @Nullable
  @Override
  public Taxonomy getTaxonomy(@Nonnull String name) {
    return orientDbService.findUnique(new Taxonomy(name));
  }

  @Override
  public void saveTaxonomy(@Nonnull final Taxonomy taxonomy) {

    // create new vocabularies
    Iterable<Vocabulary> vocabularies = filter(
        transform(taxonomy.getVocabularies(), new Function<String, Vocabulary>() {
          @Nullable
          @Override
          public Vocabulary apply(String vocabularyName) {
            Vocabulary vocabulary = getVocabulary(taxonomy.getName(), vocabularyName);
            if(vocabulary == null) {
              return new Vocabulary(taxonomy.getName(), vocabularyName);
            }
            return null;
          }
        }), Predicates.notNull());

    // delete removed vocabularies
    Taxonomy previousTaxonomy = getTaxonomy(taxonomy.getName());
    Iterable<Vocabulary> deletedVocabularies = null;
    if(previousTaxonomy != null) {
      deletedVocabularies = filter(transform(previousTaxonomy.getVocabularies(), new Function<String, Vocabulary>() {
        @Nullable
        @Override
        public Vocabulary apply(String vocabularyName) {
          return taxonomy.hasVocabulary(vocabularyName) ? null : getVocabulary(taxonomy.getName(), vocabularyName);
        }
      }), Predicates.notNull());
    }

    List<HasUniqueProperties> toSave = new ArrayList<HasUniqueProperties>();
    toSave.add(taxonomy);
    Iterables.addAll(toSave, vocabularies);

    // TODO we should execute these steps in a single transaction
    orientDbService.save(toSave.toArray(new HasUniqueProperties[toSave.size()]));
    if(deletedVocabularies != null) {
      orientDbService.delete(Iterables.toArray(deletedVocabularies, Vocabulary.class));
    }
  }

  @Override
  public void deleteTaxonomy(@Nonnull String name) {

    Taxonomy taxonomy = getTaxonomy(name);
    if(taxonomy == null) return;

    Iterable<Vocabulary> vocabularies = getVocabularies(name);

    List<HasUniqueProperties> toDelete = new ArrayList<HasUniqueProperties>();
    toDelete.add(new Taxonomy(name));
    Iterables.addAll(toDelete, vocabularies);
    orientDbService.delete(toDelete.toArray(new HasUniqueProperties[toDelete.size()]));
  }

  @Override
  public Iterable<Vocabulary> getVocabularies(@Nonnull String taxonomy) {
    return orientDbService
        .list(Vocabulary.class, "select from " + Vocabulary.class.getSimpleName() + " where taxonomy = ?", taxonomy);
  }

  @Nullable
  @Override
  public Vocabulary getVocabulary(@Nonnull String taxonomy, @Nonnull String name) {
    return orientDbService.findUnique(new Vocabulary(taxonomy, name));
  }

  @Override
  public void saveVocabulary(@Nonnull Vocabulary vocabulary) {
    Taxonomy taxonomy = getTaxonomy(vocabulary.getTaxonomy());
    if(taxonomy == null) {
      throw new IllegalArgumentException(
          "Cannot create vocabulary for non-existing taxonomy " + vocabulary.getTaxonomy());
    }
    taxonomy.addVocabulary(vocabulary.getName());
    orientDbService.save(vocabulary, taxonomy);
  }

  @Override
  public void deleteVocabulary(@Nonnull Vocabulary vocabulary) {
    Taxonomy taxonomy = getTaxonomy(vocabulary.getTaxonomy());
    if(taxonomy == null) {
      throw new IllegalArgumentException(
          "Cannot delete vocabulary for non-existing taxonomy " + vocabulary.getTaxonomy());
    }

    taxonomy.removeVocabulary(vocabulary.getName());

    // TODO we should execute these steps in a single transaction
    orientDbService.delete(vocabulary);
    orientDbService.save(taxonomy);
  }

}
