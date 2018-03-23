/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.ui.celltable;

import com.google.common.base.Strings;
import com.google.gwt.core.client.JsArray;
import org.obiba.opal.core.domain.taxonomy.Vocabulary;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.support.AttributeHelper;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.opal.TermDto;
import org.obiba.opal.web.model.client.opal.VocabularyDto;

import java.util.List;

public class VariableAnnotationColumn extends AttributeColumn<VariableDto> {

  private List<TaxonomyDto> taxonomies;

  public VariableAnnotationColumn() {
    super("*", "*");
  }


  public VariableAnnotationColumn(String taxonomyName) {
    super(taxonomyName, "*");
  }

  @Override
  protected JsArray<AttributeDto> getAttributes(VariableDto object) {
    return object.getAttributesArray();
  }

  @Override
  protected void appendLabel(AttributeDto attr, StringBuilder labels) {
    if (labels.toString().length() > 0) labels.append(" | ");
    labels.append(getTermLabel(attr));
  }

  private String getTermLabel(AttributeDto attr) {
    if (taxonomies != null) {
      for (TaxonomyDto taxonomy : taxonomies) {
        if (taxonomy.getName().equals(attr.getNamespace())) {
          String label = getTermLabel(taxonomy, attr);
          if (!Strings.isNullOrEmpty(label)) return label;
        }
      }
    }
    return attr.getValue();
  }

  private String getTermLabel(TaxonomyDto taxonomy, AttributeDto attr) {
    for (VocabularyDto vocabulary : JsArrays.toIterable(taxonomy.getVocabulariesArray())) {
      if (vocabulary.getName().equals(attr.getName())) {
        String label = getTermLabel(vocabulary, attr);
        if (!Strings.isNullOrEmpty(label)) return label;
      }
    }
    return null;
  }

  private String getTermLabel(VocabularyDto vocabulary, AttributeDto attr) {
    for (TermDto term : JsArrays.toIterable(vocabulary.getTermsArray())) {
      if (term.getName().equals(attr.getValue())) {
        return AttributeHelper.getLocaleText(term.getTitleArray());
      }
    }
    return null;
  }

    public void setTaxonomies(List<TaxonomyDto> taxonomies) {
    this.taxonomies = taxonomies;
  }
}