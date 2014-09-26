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

import java.beans.IntrospectionException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import com.google.common.collect.ImmutableSet;

public class TaxonomyTest {

  @Test
  public void test_yaml() {
    Representer repr = new TaxonomyRepresenter();
    DumperOptions options = new DumperOptions();
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    Yaml yaml = new Yaml(repr, options);

    Taxonomy taxonomy = new Taxonomy();
    taxonomy.setVersion("1.0-SNAPSHOT");
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

  private class TaxonomyPropertyUtils extends PropertyUtils {
    @Override
    protected Set<Property> createPropertySet(Class<? extends Object> type, BeanAccess bAccess)
        throws IntrospectionException {
      Map<String, Property> propertyMap = getPropertiesMap(type, BeanAccess.DEFAULT);

      ImmutableSet.Builder<Property> builder = ImmutableSet.builder();
      addProperty("name", propertyMap, builder);
      addProperty("version", propertyMap, builder);
      addProperty("title", propertyMap, builder);
      addProperty("description", propertyMap, builder);
      builder.addAll(propertyMap.values());
      return builder.build();
    }



    private void addProperty(String key, Map<String, Property> propertyMap, ImmutableSet.Builder<Property> builder) {
      if(propertyMap.containsKey(key)) {
        builder.add(propertyMap.remove(key));
      }
    }
  }

  private class TaxonomyRepresenter extends Representer {

    private TaxonomyRepresenter() {
      setPropertyUtils(new TaxonomyPropertyUtils());
    }

    @Override
    protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue,
        Tag customTag) {
      if (propertyValue == null) return null;
      if (propertyValue instanceof Boolean && !(Boolean)propertyValue) return null;
      return super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
    }
  }
}
