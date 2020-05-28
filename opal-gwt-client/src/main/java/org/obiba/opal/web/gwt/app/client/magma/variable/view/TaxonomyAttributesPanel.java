/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.magma.variable.view;

import com.github.gwtbootstrap.client.ui.HelpBlock;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.support.AttributeHelper;
import org.obiba.opal.web.gwt.app.client.ui.PropertiesTable;
import org.obiba.opal.web.gwt.markdown.client.Markdown;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.opal.TermDto;
import org.obiba.opal.web.model.client.opal.VocabularyDto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Displays the attributes described by taxonomy vocabularies.
 */
public class TaxonomyAttributesPanel extends FlowPanel {

  private final TaxonomyAttributes taxonomyAttributes = new TaxonomyAttributes();

  public TaxonomyAttributesPanel(JsArray<AttributeDto> attributes, List<TaxonomyDto> taxonomies) {
    addStyleName("top-margin");

    // group attributes by taxonomy name
    Map<String, List<AttributeDto>> namespacedAttributes = Maps.newHashMap();
    for (AttributeDto attribute : JsArrays.toIterable(attributes)) {
      if (attribute.hasNamespace()) {
        String namespace = attribute.getNamespace();
        if (!namespacedAttributes.containsKey(namespace))
          namespacedAttributes.put(namespace, new ArrayList<AttributeDto>());
        namespacedAttributes.get(namespace).add(attribute);
      }
    }

    List<String> namespaces = Lists.newArrayList(namespacedAttributes.keySet());
    Collections.sort(namespaces);
    for (String namespace : namespaces) {
      TaxonomyDto taxonomy = getTaxonomy(taxonomies, namespace);
      if (taxonomy != null) {
        // if there is a taxonomy corresponding to this namespace, display corresponding vocabulary
        showTaxonomyAttributes(taxonomy, namespacedAttributes.get(namespace));
      }
    }
  }

  public boolean isEmpty() {
    return getWidgetCount() == 0;
  }

  public TaxonomyAttributes getTaxonomyAttributes() {
    return taxonomyAttributes;
  }

  private void showTaxonomyAttributes(TaxonomyDto taxonomy, List<AttributeDto> taxoAttributes) {
    String taxoTittle = AttributeHelper.getLocaleText(taxonomy.getTitleArray());
    if (Strings.isNullOrEmpty(taxoTittle)) taxoTittle = taxonomy.getName();
    Label taxoLabel = new Label(taxoTittle);
    taxoLabel.setTitle(AttributeHelper.getLocaleText(taxonomy.getDescriptionArray()));
    add(taxoLabel);
    PropertiesTable propertiesTable = new PropertiesTable();
    add(propertiesTable);

    // group attributes by vocabulary name
    Map<String, List<AttributeDto>> namedAttributes = Maps.newHashMap();
    for (AttributeDto attribute : taxoAttributes) {
      if (!namedAttributes.containsKey(attribute.getName()))
        namedAttributes.put(attribute.getName(), new ArrayList<AttributeDto>());
      namedAttributes.get(attribute.getName()).add(attribute);
    }

    List<String> names = Lists.newArrayList(namedAttributes.keySet());
    // display in vocabulary order
    for (VocabularyDto vocabulary : JsArrays.toIterable(taxonomy.getVocabulariesArray())) {
      if (names.contains(vocabulary.getName())) {
        String name = vocabulary.getName();
        if (vocabulary.getTermsCount() == 0) {
          showVocabularyAttribute(propertiesTable, taxonomy, vocabulary, namedAttributes.get(name));
        } else {
          AttributeDto attribute = namedAttributes.get(name).get(0);
          String value = attribute.getValue();
          TermDto term = getTerm(vocabulary, value);
          if (term == null)
            showVocabularyAttribute(propertiesTable, taxonomy, vocabulary, namedAttributes.get(name));
          else
            showTermAttribute(propertiesTable, taxonomy, vocabulary, term);
        }
      }
    }
  }

  private void showVocabularyAttribute(PropertiesTable propertiesTable, TaxonomyDto taxonomy, VocabularyDto vocabulary, List<AttributeDto> attributes) {
    String currentLocale = AttributeHelper.getCurrentLanguage();
    AttributeDto attribute = null;

    // find attribute in current language or, if not found, the one without locale
    for (AttributeDto attr : attributes) {
      if (attr.hasLocale() && attr.getLocale().equals(currentLocale) && !Strings.isNullOrEmpty(attr.getValue())) {
        attribute = attr;
        break;
      } else if (!attr.hasLocale() || Strings.isNullOrEmpty(attr.getLocale()))
        attribute = attr;
    }
    if (attribute == null) return;
    // Not sure it is a good idea to add open terms
    //if (!attribute.hasLocale())
    //  taxonomyAttributes.put(taxonomy, vocabulary, attribute.getValue());

    String vocTitle = AttributeHelper.getLocaleText(vocabulary.getTitleArray());
    if (Strings.isNullOrEmpty(vocTitle)) vocTitle = vocabulary.getName();
    String vocDesc = AttributeHelper.getLocaleText(vocabulary.getDescriptionArray());
    propertiesTable.addProperty(makeWidget(vocTitle, vocDesc), new HTMLPanel(Markdown.parse((attribute.getValue()))));
  }

  private void showTermAttribute(PropertiesTable propertiesTable, TaxonomyDto taxonomy, VocabularyDto vocabulary, TermDto term) {
    String vocTitle = AttributeHelper.getLocaleText(vocabulary.getTitleArray());
    if (Strings.isNullOrEmpty(vocTitle)) vocTitle = vocabulary.getName();
    String vocDesc = AttributeHelper.getLocaleText(vocabulary.getDescriptionArray());
    String termTitle = AttributeHelper.getLocaleText(term.getTitleArray());
    if (Strings.isNullOrEmpty(termTitle)) termTitle = term.getName();
    String termDesc = AttributeHelper.getLocaleText(term.getDescriptionArray());
    propertiesTable.addProperty(makeWidget(vocTitle, vocDesc), makeWidget(termTitle, termDesc));
    taxonomyAttributes.put(taxonomy, vocabulary, term);
  }

  private Widget makeWidget(String title, String description) {
    Widget widget;
    if (Strings.isNullOrEmpty(description) || title.equals(description))
      widget = new Label(title);
    else {
      FlowPanel panel = new FlowPanel();
      panel.add(new Label(title));
      panel.add(new HelpBlock(description));
      widget = panel;
    }
    return widget;
  }

  private TaxonomyDto getTaxonomy(List<TaxonomyDto> taxonomies, String namespace) {
    for (TaxonomyDto taxonomyDto : taxonomies) {
      if (taxonomyDto.getName().equals(namespace)) return taxonomyDto;
    }
    return null;
  }

  private TermDto getTerm(VocabularyDto vocabulary, String name) {
    if (vocabulary.getTermsCount() == 0) return null;
    for (TermDto term : JsArrays.toIterable(vocabulary.getTermsArray())) {
      if (term.getName().equals(name)) return term;
    }
    return null;
  }

}
