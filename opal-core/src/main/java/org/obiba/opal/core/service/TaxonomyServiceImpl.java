/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.obiba.opal.core.cfg.TaxonomyService;
import org.obiba.opal.core.domain.HasUniqueProperties;
import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.core.domain.taxonomy.Vocabulary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

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
  public Taxonomy getTaxonomy(@NotNull String name) {
    return orientDbService.findUnique(new Taxonomy(name));
  }

  @Override
  public void saveTaxonomy(@Nullable Taxonomy template, @NotNull final Taxonomy taxonomy) {

    // create new vocabularies
    Iterable<Vocabulary> vocabularies = null;
    if(taxonomy.hasVocabularies()) {
      vocabularies = filter(transform(taxonomy.getVocabularies(), new Function<String, Vocabulary>() {
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
    }

    // delete removed vocabularies
    Iterable<Vocabulary> deletedVocabularies = null;
    if(template != null) {
      Taxonomy previousTaxonomy = orientDbService.findUnique(template);
      if(previousTaxonomy != null && previousTaxonomy.hasVocabularies()) {
        deletedVocabularies = filter(transform(previousTaxonomy.getVocabularies(), new Function<String, Vocabulary>() {
          @Nullable
          @Override
          public Vocabulary apply(String vocabularyName) {
            return taxonomy.hasVocabulary(vocabularyName) ? null : getVocabulary(taxonomy.getName(), vocabularyName);
          }
        }), Predicates.notNull());
      }
    }

    Map<HasUniqueProperties, HasUniqueProperties> toSave = Maps.newHashMap();
    toSave.put(template == null ? taxonomy : template, taxonomy);
    if(vocabularies != null) {
      for(Vocabulary vocabulary : vocabularies) {
        toSave.put(vocabulary, vocabulary);
      }
    }

    // TODO we should execute these steps in a single transaction
    orientDbService.save(toSave);
    if(deletedVocabularies != null) {
      orientDbService.delete(Iterables.toArray(deletedVocabularies, Vocabulary.class));
    }
  }

  @Override
  public void deleteTaxonomy(@NotNull String name) {

    Taxonomy taxonomy = getTaxonomy(name);
    if(taxonomy == null) return;

    Iterable<Vocabulary> vocabularies = getVocabularies(name);

    List<HasUniqueProperties> toDelete = new ArrayList<HasUniqueProperties>();
    toDelete.add(new Taxonomy(name));
    Iterables.addAll(toDelete, vocabularies);
    orientDbService.delete(toDelete.toArray(new HasUniqueProperties[toDelete.size()]));
  }

  @Override
  public Iterable<Vocabulary> getVocabularies(@NotNull String taxonomy) {
    return orientDbService
        .list(Vocabulary.class, "select from " + Vocabulary.class.getSimpleName() + " where taxonomy = ?", taxonomy);
  }

  @Nullable
  @Override
  public Vocabulary getVocabulary(@NotNull String taxonomy, @NotNull String name) {
    return orientDbService.findUnique(new Vocabulary(taxonomy, name));
  }

  @Override
  public void saveVocabulary(@Nullable Vocabulary template, @NotNull Vocabulary vocabulary) {
    Taxonomy previousTaxonomy = template == null ? null : getTaxonomy(template.getTaxonomy());
    Taxonomy taxonomy = getTaxonomy(vocabulary.getTaxonomy());
    if(taxonomy == null) {
      throw new IllegalArgumentException(
          "Cannot create vocabulary for non-existing taxonomy " + vocabulary.getTaxonomy());
    }

    Map<HasUniqueProperties, HasUniqueProperties> toSave = Maps.newHashMap();
    toSave.put(template == null ? vocabulary : template, vocabulary);
    if(Objects.equal(previousTaxonomy, taxonomy)) {
      if(template != null && !template.getName().equals(vocabulary.getName())) {
        taxonomy.renameVocabulary(template.getName(), vocabulary.getName());
        toSave.put(taxonomy, taxonomy);
      }
    } else {
      taxonomy.addVocabulary(vocabulary.getName());
      toSave.put(taxonomy, taxonomy);
      if(previousTaxonomy != null) {
        previousTaxonomy.removeVocabulary(template.getName());
        toSave.put(previousTaxonomy, previousTaxonomy);
      }
    }
    orientDbService.save(toSave);
  }

  @Override
  public void deleteVocabulary(@NotNull Vocabulary vocabulary) {
    Taxonomy taxonomy = getTaxonomy(vocabulary.getTaxonomy());
    if(taxonomy == null) {
      throw new IllegalArgumentException(
          "Cannot delete vocabulary for non-existing taxonomy " + vocabulary.getTaxonomy());
    }

    taxonomy.removeVocabulary(vocabulary.getName());

    // TODO we should execute these steps in a single transaction
    orientDbService.delete(vocabulary);
    orientDbService.save(taxonomy, taxonomy);
  }

}
