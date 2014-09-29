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

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.obiba.opal.core.cfg.NoSuchTaxonomyException;
import org.obiba.opal.core.cfg.TaxonomyService;
import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.core.domain.taxonomy.Vocabulary;
import org.obiba.opal.core.support.yaml.TaxonomyYaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@Component
public class TaxonomyServiceImpl implements TaxonomyService {

  private static final Logger log = LoggerFactory.getLogger(TaxonomyServiceImpl.class);

  private static final String MLSTRM_USER = "maelstrom-research";

  private static final String TAXONOMY_YAML = "taxonomy.yml";

  private static final String GITHUB_URL = "https://raw.githubusercontent.com";

  private final List<Taxonomy> taxonomies = Lists.newArrayList();

  @Override
  @PostConstruct
  public void start() {
    importMaelstromGitHubTaxonomy("maelstrom-taxonomies", null, "area-of-information");
    importMaelstromGitHubTaxonomy("maelstrom-taxonomies", null, "harmonization");
  }

  @Override
  public void stop() {
  }

  public void importMaelstromGitHubTaxonomy(@NotNull String repo, @Nullable String ref, String taxonomy) {
    importGitHubTaxonomy(MLSTRM_USER, repo, ref, taxonomy + "/" + TAXONOMY_YAML);
  }

  @Override
  public Taxonomy importGitHubTaxonomy(@NotNull String username, @NotNull String repo, @Nullable String ref,
      @NotNull String taxonomyFile) {
    String user = username;
    if(Strings.isNullOrEmpty(username)) user = MLSTRM_USER;
    if(Strings.isNullOrEmpty(repo)) throw new IllegalArgumentException("GitHub repository is required");
    String reference = ref;
    if(Strings.isNullOrEmpty(ref)) reference = "master";
    String fileName = taxonomyFile;
    if(Strings.isNullOrEmpty(taxonomyFile)) fileName = TAXONOMY_YAML;
    if(!fileName.endsWith(".yml")) throw new IllegalArgumentException("Taxonomy file in YAML format is required");

    String uri = GITHUB_URL + "/" + user + "/" + repo + "/" + reference + "/" + fileName;

    try {
      InputStream input = new URL(uri).openStream();
      TaxonomyYaml yaml = new TaxonomyYaml();
      Taxonomy taxonomy = yaml.load(input);
      saveTaxonomy(taxonomy);
      return taxonomy;
    } catch(Exception e) {
      log.error("Failed loading taxonomy from: " + uri, e);
      return null;
    }
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

  @Override
  public boolean hasTaxonomy(@NotNull String name) {
    for(Taxonomy taxonomy : taxonomies) {
      if(taxonomy.getName().equals(name)) return true;
    }
    return false;
  }

  @Nullable
  @Override
  public Taxonomy getTaxonomy(@NotNull String name) {
    for(Taxonomy taxonomy : taxonomies) {
      if(taxonomy.getName().equals(name)) return taxonomy;
    }
    return null;
  }

  @Override
  public void saveTaxonomy(@NotNull final Taxonomy taxonomy) {
    Taxonomy stored = getTaxonomy(taxonomy.getName());
    if(stored == null) taxonomies.add(taxonomy);
    else {
      int idx = taxonomies.indexOf(stored);
      taxonomies.set(idx, taxonomy);
    }
  }

  @Override
  public void deleteTaxonomy(@NotNull String name) {
    Taxonomy taxonomy = getTaxonomy(name);
    if(taxonomy != null) taxonomies.remove(taxonomy);
  }

  @Override
  public Iterable<Vocabulary> getVocabularies(@NotNull String name) {
    Taxonomy taxonomy = getTaxonomy(name);
    if(taxonomy == null) throw new NoSuchTaxonomyException(name);
    return taxonomy.getVocabularies();
  }

  @Override
  public boolean hasVocabulary(@NotNull String taxonomyName, @NotNull String vocabularyName)
      throws NoSuchTaxonomyException {
    Taxonomy taxonomy = getTaxonomy(taxonomyName);
    if(taxonomy == null) throw new NoSuchTaxonomyException(taxonomyName);
    return taxonomy.hasVocabulary(vocabularyName);
  }

  @Override
  public Vocabulary getVocabulary(@NotNull String taxonomyName, @NotNull String vocabularyName) {
    Taxonomy taxonomy = getTaxonomy(taxonomyName);
    if(taxonomy == null) throw new NoSuchTaxonomyException(taxonomyName);
    return taxonomy.getVocabulary(vocabularyName);
  }

  @Override
  public void saveVocabulary(@Nullable String taxonomyName, @NotNull Vocabulary vocabulary) {
    Taxonomy taxonomy = getTaxonomy(taxonomyName);
    if(taxonomy == null) throw new NoSuchTaxonomyException(taxonomyName);
    taxonomy.addVocabulary(vocabulary);
  }

  @Override
  public void deleteVocabulary(@Nullable String taxonomyName, @NotNull String vocabularyName) {
    Taxonomy taxonomy = getTaxonomy(taxonomyName);
    if(taxonomy == null) throw new NoSuchTaxonomyException(taxonomyName);
    taxonomy.removeVocabulary(vocabularyName);
  }

}
