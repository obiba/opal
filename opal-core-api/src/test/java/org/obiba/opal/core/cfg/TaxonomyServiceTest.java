package org.obiba.opal.core.cfg;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.core.domain.taxonomy.Term;
import org.obiba.opal.core.domain.taxonomy.Text;
import org.obiba.opal.core.domain.taxonomy.Vocabulary;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TaxonomyServiceTest {

  @Autowired
  private TaxonomyService taxonomyService;

  @Test
  public void testAll() throws Exception {

    Taxonomy taxonomy = new Taxonomy("maelstrom");
    Term root = new Term("datasource");
    List<Term> terms = new ArrayList<Term>();
    terms.add(new Term("administrative information"));
    terms.add(new Term("biosample"));
    terms.add(new Term("physical measures"));
    terms.add(new Term("questionnaire"));
    root.setTerms(terms);

    List<Text> titles = new ArrayList<Text>();
    titles.add(new Text("Data source", "en"));
    titles.add(new Text("Source de donn�es", "fr"));
    root.setTitles(titles);

    List<Text> descriptions = new ArrayList<Text>();
    descriptions.add(new Text("Indicator of the source of the variable", "en"));
    descriptions.add(new Text("Indicateur de la source des donn�es", "fr"));
    root.setDescriptions(descriptions);

    Vocabulary vocabulary = new Vocabulary(root);
    vocabulary.setRepeatable(true);
    taxonomy.addTitle(new Text("Maelstrom research taxonomy", "en"));
    taxonomy.addDescription(new Text("The maelstrom research taxonomy is blabla", "en"));
    taxonomy.add(vocabulary);

    taxonomyService.addOrReplaceTaxonomy(taxonomy);
    Taxonomy savedTaxonomy = taxonomyService.getTaxonomy(taxonomy.getName());
    Vocabulary savedVocabulary = savedTaxonomy.getVocabularies().get(0);

    assertThat(taxonomy.getName(), is(savedTaxonomy.getName()));
    assertThat(taxonomyService.getTaxonomies().size(), is(0));
    assertThat(savedVocabulary.getRoot().getTerms().containsAll(terms), is(true));
    assertThat(savedVocabulary.getRoot().getTitles().containsAll(titles), is(true));
  }
}
