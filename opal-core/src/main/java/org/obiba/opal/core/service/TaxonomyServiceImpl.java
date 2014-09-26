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

import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.obiba.opal.core.cfg.NoSuchTaxonomyException;
import org.obiba.opal.core.cfg.TaxonomyService;
import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.core.domain.taxonomy.Vocabulary;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class TaxonomyServiceImpl implements TaxonomyService {

  private final List<Taxonomy> taxonomies = Lists.newArrayList();

  @Override
  @PostConstruct
  public void start() {

  }

  @Override
  public void stop() {
  }

  /**
   * For testing.
   */
  void clear() {
    taxonomies.clear();
  }

  @Override
  public Iterable<Taxonomy> getTaxonomies() {
    return taxonomies;
  }

  @Nullable
  @Override
  public Taxonomy getTaxonomy(@NotNull String name) {
    for (Taxonomy taxonomy : taxonomies) {
      if (taxonomy.getName().equals(name)) return taxonomy;
    }
    return null;
  }

  @Override
  public void saveTaxonomy(@NotNull final Taxonomy taxonomy) {
    Taxonomy stored = getTaxonomy(taxonomy.getName());
    if (stored == null) taxonomies.add(taxonomy);
    else {
      int idx = taxonomies.indexOf(stored);
      taxonomies.set(idx, taxonomy);
    }
  }

  @Override
  public void deleteTaxonomy(@NotNull String name) {
    Taxonomy taxonomy = getTaxonomy(name);
    if (taxonomy != null) taxonomies.remove(taxonomy);
  }

  @Override
  public Iterable<Vocabulary> getVocabularies(@NotNull String name) {
    Taxonomy taxonomy = getTaxonomy(name);
    if (taxonomy == null) throw new NoSuchTaxonomyException(name);
    return taxonomy.getVocabularies();
  }

  @Override
  public boolean hasVocabulary(@NotNull String taxonomyName, @NotNull String vocabularyName) throws NoSuchTaxonomyException {
    Taxonomy taxonomy = getTaxonomy(taxonomyName);
    if (taxonomy == null) throw new NoSuchTaxonomyException(taxonomyName);
    return taxonomy.hasVocabulary(vocabularyName);
  }

  @Override
  public Vocabulary getVocabulary(@NotNull String taxonomyName, @NotNull String vocabularyName) {
    Taxonomy taxonomy = getTaxonomy(taxonomyName);
    if (taxonomy == null) throw new NoSuchTaxonomyException(taxonomyName);
    return taxonomy.getVocabulary(vocabularyName);
  }

  @Override
  public void saveVocabulary(@Nullable String taxonomyName, @NotNull Vocabulary vocabulary) {
    Taxonomy taxonomy = getTaxonomy(taxonomyName);
    if (taxonomy == null) throw new NoSuchTaxonomyException(taxonomyName);
    taxonomy.addVocabulary(vocabulary);
  }

  @Override
  public void deleteVocabulary(@Nullable String taxonomyName, @NotNull String vocabularyName) {
    Taxonomy taxonomy = getTaxonomy(taxonomyName);
    if (taxonomy == null) throw new NoSuchTaxonomyException(taxonomyName);
    taxonomy.removeVocabulary(vocabularyName);
  }

}
