/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.domain.taxonomy;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;

import org.junit.Test;
import org.obiba.opal.core.support.yaml.TaxonomyYaml;
import org.yaml.snakeyaml.Yaml;

import static org.fest.assertions.api.Assertions.assertThat;

public class TaxonomyTest {

  @Test
  public void test_yaml_dump() {
    Yaml yaml = new TaxonomyYaml();

    Taxonomy taxonomy = new Taxonomy();
    taxonomy.setName("mlst_area");
    taxonomy.addTitle(Locale.ENGLISH, "Maelstrom - Area of Information");
    taxonomy.addTitle(Locale.FRENCH, "Maelstrom - Domaines d'information");
    taxonomy.addDescription(Locale.ENGLISH, "Area of Information");
    taxonomy.addDescription(Locale.FRENCH, "Domaines d'information");

    Vocabulary vocabulary = new Vocabulary("socio_demo_eco");
    vocabulary.addTitle(Locale.ENGLISH, "Socio-demographic and economic characteristics");
    vocabulary
        .addDescription(Locale.ENGLISH, "Refers to sociodemographic or socioeconomic characteristics of an individual");
    taxonomy.addVocabulary(vocabulary);

    Term term = new Term("age");
    term.addTitle(Locale.ENGLISH, "Age / Birth Date");
    term.addDescription(Locale.ENGLISH, "Information about current age (e.g. when participating in the study)");
    vocabulary.addTerm(term);

    vocabulary = new Vocabulary("lifestyle");
    vocabulary.addTitle(Locale.ENGLISH, "Lifestyle and health behaviours");
    vocabulary.addDescription(Locale.ENGLISH,
        "Refers to information about past and current lifestyle, behaviours, activities and life plans");
    taxonomy.addVocabulary(vocabulary);

    term = new Term("tobacco");
    term.addTitle(Locale.ENGLISH, "Tobacco");
    term.addDescription(Locale.ENGLISH,
        "Information about the use of tobacco in any form (e.g. smoking, chewing or sniffing frequency and quantity) and associated behaviours");
    vocabulary.addTerm(term);

    term = new Term("alcohol");
    term.addTitle(Locale.ENGLISH, "Alcohol");
    term.addDescription(Locale.ENGLISH,
        "Information about the consumption of alcoholic beverages (e.g. frequency, quantity and type of alcohol consumed) and associated behaviours");
    vocabulary.addTerm(term);

    System.out.println(yaml.dump(taxonomy));
  }

  @Test
  public void test_yaml_read() {
    try {
      InputStream input = new URL(
          "https://raw.githubusercontent.com/maelstrom-research/maelstrom-taxonomies/master/area-of-information/taxonomy.yml")
          .openStream();
      TaxonomyYaml yaml = new TaxonomyYaml();
      Taxonomy taxonomy = yaml.load(input);
      assertThat(taxonomy).isNotNull();
    } catch(IOException e) {
      e.printStackTrace();
    }
  }

}
