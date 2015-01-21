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

import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.obiba.opal.core.cfg.NoSuchTaxonomyException;
import org.obiba.opal.core.cfg.NoSuchVocabularyException;
import org.obiba.opal.core.cfg.TaxonomyService;
import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.core.domain.taxonomy.Term;
import org.obiba.opal.core.domain.taxonomy.Vocabulary;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import static com.google.common.collect.Lists.newArrayList;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;

@ContextConfiguration(classes = TaxonomyServiceImplTest.Config.class)
public class TaxonomyServiceImplTest extends AbstractJUnit4SpringContextTests {

  private static final Logger log = LoggerFactory.getLogger(TaxonomyServiceImplTest.class);

  @Autowired
  private TaxonomyService taxonomyService;

  @Rule
  public TestWatcher watchman = new TestWatcher() {
    @Override
    protected void starting(Description description) {
      log.info(">>> Run test {}", description.getMethodName());
    }
  };

  @Before
  public void clear() {
    ((TaxonomyServiceImpl)taxonomyService).clear();
  }

  @Test
  public void test_create_new_taxonomy() {
    Taxonomy taxonomy = createTaxonomy();
    taxonomyService.saveTaxonomy(taxonomy);

    List<Taxonomy> taxonomies = newArrayList(taxonomyService.getTaxonomies());
    assertThat(taxonomies).hasSize(1);

    assertTaxonomyEquals(taxonomy, taxonomies.get(0));
    assertTaxonomyEquals(taxonomy, taxonomyService.getTaxonomy(taxonomy.getName()));

    List<Vocabulary> vocabularies = newArrayList(taxonomyService.getVocabularies(taxonomy.getName()));
    assertThat(vocabularies).hasSize(1);

    Vocabulary expected = new Vocabulary("vocabulary 1");
    assertVocabularyEquals(expected, vocabularies.get(0));

    Vocabulary found = taxonomyService.getVocabulary(taxonomy.getName(), expected.getName());
    assertVocabularyEquals(expected, found);
    assertVocabularyEquals(vocabularies.get(0), found);
  }

  @Test
  public void test_add_vocabulary() {
    Taxonomy taxonomy = createTaxonomy();
    taxonomyService.saveTaxonomy(taxonomy);
    taxonomyService.saveVocabulary(taxonomy.getName(), new Vocabulary("vocabulary 2"));

    assertTaxonomyEquals(taxonomy, taxonomyService.getTaxonomy(taxonomy.getName()));

    assertThat(taxonomyService.getTaxonomies()).hasSize(1);
    assertThat(taxonomyService.getVocabularies(taxonomy.getName())).hasSize(2);
  }

  @Test
  public void test_delete_taxonomy() {
    Taxonomy taxonomy = createTaxonomy();
    taxonomyService.saveTaxonomy(taxonomy);
    assertThat(taxonomyService.getTaxonomies()).hasSize(1);

    taxonomyService.deleteTaxonomy(taxonomy.getName());
    assertThat(taxonomyService.getTaxonomies()).isEmpty();
  }

  @Test
  public void test_save_vocabulary() {
    Taxonomy taxonomy = createTaxonomy();
    taxonomyService.saveTaxonomy(taxonomy);
    assertThat(taxonomyService.getTaxonomies()).hasSize(1);

    Vocabulary vocabulary = createVocabulary();
    taxonomyService.saveVocabulary(taxonomy.getName(), vocabulary);

    Taxonomy foundTaxonomy = taxonomyService.getTaxonomy(taxonomy.getName());
    assertThat(foundTaxonomy).isNotNull();
    assertThat(foundTaxonomy.hasVocabulary(vocabulary.getName())).isTrue();

    Vocabulary foundVocabulary = taxonomyService.getVocabulary(taxonomy.getName(), vocabulary.getName());
    assertThat(foundVocabulary).isNotNull();
    assertVocabularyEquals(vocabulary, foundVocabulary);

    foundVocabulary.getTerms().clear();
    foundVocabulary.addTerm(createTerm("new term"));
    taxonomyService.saveVocabulary(taxonomy.getName(), foundVocabulary);

    Vocabulary foundVocabulary2 = taxonomyService.getVocabulary(taxonomy.getName(), foundVocabulary.getName());
    assertThat(foundVocabulary2).isNotNull();
    assertVocabularyEquals(foundVocabulary, foundVocabulary2);
  }

  @Test(expected = NoSuchTaxonomyException.class)
  public void test_save_vocabulary_without_taxonomy() {
    taxonomyService.saveVocabulary("patate", new Vocabulary("voc1"));
  }

  @Test
  public void test_delete_vocabulary() {
    Taxonomy taxonomy = createTaxonomy();
    taxonomyService.saveTaxonomy(taxonomy);

    Vocabulary vocabulary = createVocabulary();
    taxonomyService.saveVocabulary(taxonomy.getName(), vocabulary);
    taxonomyService.deleteVocabulary(taxonomy.getName(), vocabulary.getName());

    Taxonomy foundTaxonomy = taxonomyService.getTaxonomy(taxonomy.getName());
    assertThat(foundTaxonomy).isNotNull();
    assertThat(foundTaxonomy.hasVocabulary(vocabulary.getName())).isFalse();

    assertThat(taxonomyService.hasVocabulary(taxonomy.getName(), vocabulary.getName())).isFalse();
  }

  @Test
  public void test_remove_vocabulary_from_taxonomy() {
    Taxonomy taxonomy = createTaxonomy();
    taxonomyService.saveTaxonomy(taxonomy);

    taxonomy.removeVocabulary("vocabulary 1");
    taxonomyService.saveTaxonomy(taxonomy);

    assertTaxonomyEquals(taxonomy, taxonomyService.getTaxonomy(taxonomy.getName()));
    assertThat(taxonomyService.getTaxonomies()).hasSize(1);
    assertThat(taxonomyService.getVocabularies(taxonomy.getName())).isEmpty();
  }

  @Test
  public void test_delete_remove_rename_vocabulary() {
    Taxonomy taxonomy = new Taxonomy("taxonomy");
    taxonomyService.saveTaxonomy(taxonomy);

    Vocabulary vocabulary = new Vocabulary("vocabulary 1");
    taxonomyService.saveVocabulary(taxonomy.getName(), vocabulary);

    Vocabulary vocabulary2 = new Vocabulary("vocabulary 2");
    vocabulary2.setName("vocabulary 2");
    taxonomyService.saveVocabulary(taxonomy.getName(), vocabulary2);

    taxonomyService.deleteVocabulary(taxonomy.getName(), vocabulary.getName());

    assertThat(taxonomyService.hasVocabulary(taxonomy.getName(), vocabulary.getName())).isFalse();
    try {
      taxonomyService.getVocabulary(taxonomy.getName(), "vocabulary 1");
      fail("Vocabulary not deleted");
    } catch (NoSuchVocabularyException e) {
    }

    Vocabulary vocabulary3 = new Vocabulary("vocabulary 1");
    taxonomyService.saveVocabulary(taxonomy.getName(), vocabulary3);

    Vocabulary found = taxonomyService.getVocabulary(taxonomy.getName(), "vocabulary 1");
    assertThat(found).isNotNull();
  }

  private Taxonomy createTaxonomy() {
    Taxonomy taxonomy = new Taxonomy("taxonomy test");
    taxonomy.addTitle(Locale.ENGLISH, "English title") //
        .addTitle(Locale.FRENCH, "Titre francais") //
        .addDescription(Locale.ENGLISH, "English description") //
        .addDescription(Locale.FRENCH, "Description francais");
    taxonomy.addVocabulary(new Vocabulary("vocabulary 1"));
    return taxonomy;
  }

  private Vocabulary createVocabulary() {
    Vocabulary vocabulary = new Vocabulary("vocabulary test");

    vocabulary.addTitle(Locale.ENGLISH, "English vocabulary title") //
        .addTitle(Locale.FRENCH, "Titre vocabulaire francais") //
        .addDescription(Locale.ENGLISH, "English vocabulary description") //
        .addDescription(Locale.FRENCH, "Description vocabulaire francais");

    vocabulary.addTerm(createTerm("1")) //
        .addTerm(createTerm("2"));

    return vocabulary;
  }

  private Term createTerm(String suffix) {
    Term term = new Term("term " + suffix);
    term.addTitle(Locale.ENGLISH, "English title " + suffix) //
        .addTitle(Locale.FRENCH, "Titre francais " + suffix) //
        .addDescription(Locale.ENGLISH, "English description " + suffix) //
        .addDescription(Locale.FRENCH, "Description francais " + suffix);
    return term;
  }

  private void assertTaxonomyEquals(Taxonomy expected, Taxonomy found) {
    assertThat(found).isNotNull();

    assertThat(expected).isEqualTo(found);
    assertThat(expected.getTitle()).isEqualTo(found.getTitle());
    assertThat(expected.getDescription()).isEqualTo(found.getDescription());
    assertThat(expected.getVocabularies()).isEqualTo(found.getVocabularies());
  }

  private void assertVocabularyEquals(Vocabulary expected, Vocabulary found) {
    assertThat(found).isNotNull();

    assertThat(expected).isEqualTo(found);
    assertThat(expected.isRepeatable()).isEqualTo(found.isRepeatable());
    assertThat(expected.getTitle()).isEqualTo(found.getTitle());
    assertThat(expected.getDescription()).isEqualTo(found.getDescription());
    assertThat(expected.getTerms()).isEqualTo(found.getTerms());
  }

  @Configuration
  @PropertySource("classpath:/META-INF/defaults.properties")
  public static class Config extends AbstractOrientDbTestConfig {

    @Bean
    public TaxonomyService taxonomyService() {
      return new TaxonomyServiceImpl();
    }

    @Bean
    public OpalRuntime opalRuntime() {
      OpalRuntime mock = EasyMock.createMock(OpalRuntime.class);
      EasyMock.expect(mock.hasFileSystem()).andReturn(false).anyTimes();
      EasyMock.replay(mock);
      return mock;
    }

    @Bean
    public TaxonomyPersistenceStrategy taxonomyPersistence() {
      TaxonomyPersistenceStrategy mock = EasyMock.createMock(TaxonomyPersistenceStrategy.class);
      mock.writeTaxonomy(EasyMock.anyString(), EasyMock.anyObject(Taxonomy.class), EasyMock.anyString());
      EasyMock.expectLastCall().anyTimes();
      mock.removeTaxonomy(EasyMock.anyString(), EasyMock.anyString());
      EasyMock.expectLastCall().anyTimes();
      EasyMock.expect(mock.readTaxonomies()).andReturn(new HashSet<Taxonomy>());
      EasyMock.replay(mock);
      return mock;
    }

  }
}
