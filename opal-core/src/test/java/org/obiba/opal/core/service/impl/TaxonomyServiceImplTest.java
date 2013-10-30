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

import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.core.cfg.TaxonomyService;
import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.core.domain.taxonomy.Vocabulary;
import org.obiba.opal.core.service.OrientDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@ContextConfiguration(classes = TestTaxonomyServiceConfig.class)
public class TaxonomyServiceImplTest extends AbstractJUnit4SpringContextTests {

  @Autowired
  private TaxonomyService taxonomyService;

  @Autowired
  private OrientDbService orientDbService;

  @Before
  public void clear() {
    orientDbService.delete("select from " + Taxonomy.class.getSimpleName());
    orientDbService.delete("select from " + Vocabulary.class.getSimpleName());
  }

  @Test
  public void test_create_taxonomy() {

    Taxonomy taxonomy = createTaxonomy();
    taxonomyService.saveTaxonomy(taxonomy);

    List<Taxonomy> taxonomies = Lists.newArrayList(taxonomyService.getTaxonomies());
    assertEquals(1, taxonomies.size());

    assertTaxonomyEquals(taxonomy, taxonomies.get(0));
    assertTaxonomyEquals(taxonomy, taxonomyService.getTaxonomy(taxonomy.getName()));

    List<Vocabulary> vocabularies = Lists.newArrayList(taxonomyService.getVocabularies(taxonomy.getName()));
    assertEquals(1, vocabularies.size());

    Vocabulary expected = new Vocabulary(taxonomy.getName(), "vocabulary 1");
    assertVocabularyEquals(expected, vocabularies.get(0));

    Vocabulary found = taxonomyService.getVocabulary(taxonomy.getName(), expected.getName());
    assertVocabularyEquals(expected, found);
    assertVocabularyEquals(vocabularies.get(0), found);
  }

  @Test
  public void test_update_taxonomy_name() {
    Taxonomy taxonomy = createTaxonomy();
    taxonomyService.saveTaxonomy(taxonomy);

    taxonomy.setName("sdf");

  }

  @Test
  public void test_add_vocabulary() {
    Taxonomy taxonomy = createTaxonomy();
    taxonomyService.saveTaxonomy(taxonomy);

    taxonomy.addVocabulary("vocabulary 2");
    taxonomyService.saveTaxonomy(taxonomy);

    assertTaxonomyEquals(taxonomy, taxonomyService.getTaxonomy(taxonomy.getName()));

    List<Taxonomy> taxonomies = Lists.newArrayList(taxonomyService.getTaxonomies());
    assertEquals(1, taxonomies.size());

    List<Vocabulary> vocabularies = Lists.newArrayList(taxonomyService.getVocabularies(taxonomy.getName()));
    assertEquals(2, vocabularies.size());
  }

  @Test
  public void test_remove_vocabulary() {
    Taxonomy taxonomy = createTaxonomy();
    taxonomyService.saveTaxonomy(taxonomy);

    taxonomy.removeVocabulary("vocabulary 1");
    taxonomyService.saveTaxonomy(taxonomy);

    assertTaxonomyEquals(taxonomy, taxonomyService.getTaxonomy(taxonomy.getName()));

    List<Taxonomy> taxonomies = Lists.newArrayList(taxonomyService.getTaxonomies());
    assertEquals(1, taxonomies.size());

    List<Vocabulary> vocabularies = Lists.newArrayList(taxonomyService.getVocabularies(taxonomy.getName()));
    Assert.isTrue(vocabularies.isEmpty());
  }

  private Taxonomy createTaxonomy() {
    Taxonomy taxonomy = new Taxonomy("taxonomy test");
    taxonomy.addTitle(Locale.ENGLISH, "English title");
    taxonomy.addTitle(Locale.FRENCH, "Titre francais");
    taxonomy.addDescription(Locale.ENGLISH, "English description");
    taxonomy.addDescription(Locale.FRENCH, "Description francais");
    taxonomy.addVocabulary("vocabulary 1");
    return taxonomy;
  }

  private void assertTaxonomyEquals(Taxonomy expected, Taxonomy found) {
    assertNotNull(found);
    assertEquals(expected, found);
    assertEquals(expected.getTitles(), found.getTitles());
    assertEquals(expected.getDescriptions(), found.getDescriptions());
    assertEquals(expected.getVocabularies(), found.getVocabularies());
  }

  private void assertVocabularyEquals(Vocabulary expected, Vocabulary found) {
    assertNotNull(found);
    assertEquals(expected, found);
    assertEquals(expected.getTaxonomy(), found.getTaxonomy());
    assertEquals(expected.isRepeatable(), found.isRepeatable());
    assertEquals(expected.getTitles(), found.getTitles());
    assertEquals(expected.getDescriptions(), found.getDescriptions());
    assertEquals(expected.getTerms(), found.getTerms());
  }

}
