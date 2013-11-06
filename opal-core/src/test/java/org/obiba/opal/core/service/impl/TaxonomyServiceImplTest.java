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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.obiba.opal.core.cfg.TaxonomyService;
import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.core.domain.taxonomy.Term;
import org.obiba.opal.core.domain.taxonomy.Vocabulary;
import org.obiba.opal.core.service.OrientDbService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.google.common.collect.Iterables;

import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(classes = TaxonomyServiceImplTest.Config.class)
public class TaxonomyServiceImplTest extends AbstractJUnit4SpringContextTests {

  private static final Logger log = LoggerFactory.getLogger(TaxonomyServiceImplTest.class);

  @Autowired
  private TaxonomyService taxonomyService;

  @Autowired
  private OrientDbService orientDbService;

  @Rule
  public TestWatcher watchman = new TestWatcher() {
    @Override
    protected void starting(Description description) {
      log.info(">>> Run test {}", description.getMethodName());
    }
  };

  @Before
  public void clear() {
    orientDbService.deleteAll(Taxonomy.class);
    orientDbService.deleteAll(Vocabulary.class);
  }

  @Test
  public void test_create_new_taxonomy() {
    Taxonomy taxonomy = createTaxonomy();
    taxonomyService.saveTaxonomy(taxonomy, taxonomy);

    List<Taxonomy> taxonomies = newArrayList(taxonomyService.getTaxonomies());
    assertEquals(1, taxonomies.size());

    assertTaxonomyEquals(taxonomy, taxonomies.get(0));
    assertTaxonomyEquals(taxonomy, taxonomyService.getTaxonomy(taxonomy.getName()));

    List<Vocabulary> vocabularies = newArrayList(taxonomyService.getVocabularies(taxonomy.getName()));
    assertEquals(1, vocabularies.size());

    Vocabulary expected = new Vocabulary(taxonomy.getName(), "vocabulary 1");
    assertVocabularyEquals(expected, vocabularies.get(0));

    Vocabulary found = taxonomyService.getVocabulary(taxonomy.getName(), expected.getName());
    assertVocabularyEquals(expected, found);
    assertVocabularyEquals(vocabularies.get(0), found);
  }

  @Test
  public void test_save_instead_of_update_taxonomy_name() {
    Taxonomy taxonomy = createTaxonomy();
    taxonomyService.saveTaxonomy(taxonomy, taxonomy);

    taxonomy.setName("new name");
    taxonomyService.saveTaxonomy(taxonomy, taxonomy);

    assertEquals(2, size(taxonomyService.getTaxonomies()));
  }

  @Test
  public void test_update_taxonomy_name() {
    Taxonomy taxonomy = createTaxonomy();
    taxonomyService.saveTaxonomy(taxonomy, taxonomy);

    taxonomy.setName("new name");
    taxonomyService.saveTaxonomy(createTaxonomy(), taxonomy);

    assertEquals(1, size(taxonomyService.getTaxonomies()));

    assertTaxonomyEquals(taxonomy, taxonomyService.getTaxonomy(taxonomy.getName()));
  }

  @Test
  public void test_add_vocabulary() {
    Taxonomy taxonomy = createTaxonomy();
    taxonomyService.saveTaxonomy(taxonomy, taxonomy);

    taxonomy.addVocabulary("vocabulary 2");
    taxonomyService.saveTaxonomy(taxonomy, taxonomy);

    assertTaxonomyEquals(taxonomy, taxonomyService.getTaxonomy(taxonomy.getName()));

    assertEquals(1, size(taxonomyService.getTaxonomies()));
    assertEquals(2, size(taxonomyService.getVocabularies(taxonomy.getName())));

  }

  @Test
  public void test_delete_taxonomy() {
    Taxonomy taxonomy = createTaxonomy();
    taxonomyService.saveTaxonomy(taxonomy, taxonomy);
    assertEquals(1, size(taxonomyService.getTaxonomies()));

    taxonomyService.deleteTaxonomy(taxonomy.getName());
    assertEquals(0, size(taxonomyService.getTaxonomies()));
    assertEquals(0, size(orientDbService.list(Taxonomy.class, "select from " + Taxonomy.class.getSimpleName())));
    assertEquals(0, orientDbService.count(Taxonomy.class));
  }

  @Test
  public void test_save_vocabulary() {
    Taxonomy taxonomy = createTaxonomy();
    taxonomyService.saveTaxonomy(taxonomy, taxonomy);
    assertEquals(1, size(taxonomyService.getTaxonomies()));

    Vocabulary vocabulary = createVocabulary(taxonomy);
    taxonomyService.saveVocabulary(null, vocabulary);

    Taxonomy foundTaxonomy = taxonomyService.getTaxonomy(taxonomy.getName());
    assertNotNull(foundTaxonomy);
    assertTrue(foundTaxonomy.hasVocabulary(vocabulary.getName()));

    Vocabulary foundVocabulary = taxonomyService.getVocabulary(taxonomy.getName(), vocabulary.getName());
    assertNotNull(foundVocabulary);
    assertVocabularyEquals(vocabulary, foundVocabulary);

    foundVocabulary.getTerms().clear();
    foundVocabulary.addTerm(createTerm("new term"));
    taxonomyService.saveVocabulary(foundVocabulary, foundVocabulary);

    Vocabulary foundVocabulary2 = taxonomyService.getVocabulary(taxonomy.getName(), foundVocabulary.getName());
    assertNotNull(foundVocabulary2);
    assertVocabularyEquals(foundVocabulary, foundVocabulary2);
  }

  @Test
  public void test_rename_taxonomy_with_vocabulary() {
    Taxonomy taxonomy = createTaxonomy();
    taxonomyService.saveTaxonomy(taxonomy, taxonomy);

    Vocabulary vocabulary = createVocabulary(taxonomy);
    taxonomyService.saveVocabulary(null, vocabulary);

    Taxonomy foundTaxonomy = taxonomyService.getTaxonomy(taxonomy.getName());
    foundTaxonomy.setName("new name");
    taxonomyService.saveTaxonomy(foundTaxonomy, foundTaxonomy);

    foundTaxonomy = taxonomyService.getTaxonomy("new name");
    assertNotNull(foundTaxonomy);
    assertTrue(foundTaxonomy.hasVocabulary(vocabulary.getName()));
  }

  @Test
  public void test_save_vocabulary_without_taxonomy() {
    try {
      taxonomyService.saveVocabulary(null, new Vocabulary("none", "voc1"));
      Assert.fail("Should throw IllegalArgumentException");
    } catch(Exception e) {
    }
  }

  @Test
  public void test_delete_vocabulary() {
    Taxonomy taxonomy = createTaxonomy();
    taxonomyService.saveTaxonomy(taxonomy, taxonomy);

    Vocabulary vocabulary = createVocabulary(taxonomy);
    taxonomyService.saveVocabulary(null, vocabulary);
    taxonomyService.deleteVocabulary(vocabulary);

    Taxonomy foundTaxonomy = taxonomyService.getTaxonomy(taxonomy.getName());
    assertNotNull(foundTaxonomy);
    assertFalse(foundTaxonomy.hasVocabulary(vocabulary.getName()));

    Vocabulary foundVocabulary = taxonomyService.getVocabulary(taxonomy.getName(), vocabulary.getName());
    assertNull(foundVocabulary);
  }

  @Test
  public void test_remove_vocabulary_from_taxonomy() {
    Taxonomy taxonomy = createTaxonomy();
    taxonomyService.saveTaxonomy(taxonomy, taxonomy);

    taxonomy.removeVocabulary("vocabulary 1");
    taxonomyService.saveTaxonomy(taxonomy, taxonomy);

    assertTaxonomyEquals(taxonomy, taxonomyService.getTaxonomy(taxonomy.getName()));
    assertEquals(1, size(taxonomyService.getTaxonomies()));
    assertTrue(Iterables.isEmpty(taxonomyService.getVocabularies(taxonomy.getName())));
  }

  @Test
  public void test_move_vocabulary() {
    Taxonomy taxonomy = createTaxonomy();
    taxonomyService.saveTaxonomy(taxonomy, taxonomy);

    Vocabulary vocabulary = createVocabulary(taxonomy);
    taxonomyService.saveVocabulary(null, vocabulary);

    Taxonomy taxonomy1 = createTaxonomy();
    taxonomy1.setName("taxonomy 1");
    taxonomyService.saveTaxonomy(taxonomy1, taxonomy1);

    // Move vocabulary
    Vocabulary template = new Vocabulary(vocabulary.getTaxonomy(), vocabulary.getName());
    vocabulary.setTaxonomy(taxonomy1.getName());
    taxonomyService.saveVocabulary(template, vocabulary);

    Taxonomy foundTaxonomy = taxonomyService.getTaxonomy(taxonomy.getName());
    assertNotNull(foundTaxonomy);
    assertFalse(foundTaxonomy.hasVocabulary(vocabulary.getName()));

    Taxonomy foundTaxonomy1 = taxonomyService.getTaxonomy(taxonomy1.getName());
    assertNotNull(foundTaxonomy1);
    assertTrue(foundTaxonomy1.hasVocabulary(vocabulary.getName()));

  }

  private Taxonomy createTaxonomy() {
    return new Taxonomy("taxonomy test") //
        .addTitle(Locale.ENGLISH, "English title") //
        .addTitle(Locale.FRENCH, "Titre francais") //
        .addDescription(Locale.ENGLISH, "English description") //
        .addDescription(Locale.FRENCH, "Description francais") //
        .addVocabulary("vocabulary 1");
  }

  private Vocabulary createVocabulary(Taxonomy taxonomy) {
    return new Vocabulary(taxonomy.getName(), "vocabulary test") //
        .addTitle(Locale.ENGLISH, "English vocabulary title") //
        .addTitle(Locale.FRENCH, "Titre vocabulaire francais") //
        .addDescription(Locale.ENGLISH, "English vocabulary description") //
        .addDescription(Locale.FRENCH, "Description vocabulaire francais")//
        .addTerm(createTerm("1").addTerm(createTerm("1.1"))) //
        .addTerm(createTerm("2"));
  }

  private Term createTerm(String suffix) {
    return new Term("term " + suffix) //
        .addTitle(Locale.ENGLISH, "English title " + suffix) //
        .addTitle(Locale.FRENCH, "Titre francais " + suffix) //
        .addDescription(Locale.ENGLISH, "English description " + suffix) //
        .addDescription(Locale.FRENCH, "Description francais " + suffix);
  }

  private void assertTaxonomyEquals(Taxonomy expected, Taxonomy found) {
    assertNotNull(found);
    assertEquals(expected, found);
    assertEquals(expected.getTitles(), found.getTitles());
    assertEquals(expected.getDescriptions(), found.getDescriptions());
    assertEquals(expected.getVocabularies(), found.getVocabularies());
    Asserts.assertCreatedTimestamps(expected, found);
  }

  private void assertVocabularyEquals(Vocabulary expected, Vocabulary found) {
    assertNotNull(found);
    assertEquals(expected, found);
    assertEquals(expected.getTaxonomy(), found.getTaxonomy());
    assertEquals(expected.isRepeatable(), found.isRepeatable());
    assertEquals(expected.getTitles(), found.getTitles());
    assertEquals(expected.getDescriptions(), found.getDescriptions());
    assertEquals(expected.getTerms(), found.getTerms());
    Asserts.assertCreatedTimestamps(expected, found);
  }

  @Configuration
  public static class Config extends AbstractOrientDbTestConfig {

    @Bean
    public TaxonomyService taxonomyService() {
      return new TaxonomyServiceImpl();
    }

  }
}
