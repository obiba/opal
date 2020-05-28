/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
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
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.support.AttributeHelper;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.opal.TermDto;
import org.obiba.opal.web.model.client.opal.VocabularyDto;

import java.util.List;

public class VariableAnnotationColumn extends AttributeColumn<VariableDto> {

  private TableDto table;

  private List<TaxonomyDto> taxonomies;

  public VariableAnnotationColumn() {
    super("*", "*");
  }

  @Override
  protected JsArray<AttributeDto> getAttributes(VariableDto object) {
    return object.getAttributesArray();
  }

  @Override
  protected void appendLabel(AttributeDto attr, StringBuilder labels) {
    String label = getTermLabel(attr);
    if (!Strings.isNullOrEmpty(label)) {
      if (labels.toString().length() > 0) labels.append(" | ");
      labels.append(label);
    }
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
    return null;
  }

  private String getTermLabel(TaxonomyDto taxonomy, AttributeDto attr) {
    for (VocabularyDto vocabulary : JsArrays.toIterable(taxonomy.getVocabulariesArray())) {
      if (vocabulary.getName().equals(attr.getName())) {
        String label = getTermLabel(taxonomy, vocabulary, attr);
        if (!Strings.isNullOrEmpty(label)) return label;
      }
    }
    return null;
  }

  private String getTermLabel(TaxonomyDto taxonomy, VocabularyDto vocabulary, AttributeDto attr) {
    for (TermDto term : JsArrays.toIterable(vocabulary.getTermsArray())) {
      if (term.getName().equals(attr.getValue())) {
        return "<a href='#!search/!variables;rq=" + getRQL(taxonomy, vocabulary, term) + "'>" + AttributeHelper.getLocaleText(term.getTitleArray()) + "</a>";
      }
    }
    return null;
  }

  public void initialize(TableDto table, List<TaxonomyDto> taxonomies) {
    this.table = table;
    this.taxonomies = taxonomies;
  }

  private String getRQL(TaxonomyDto taxonomy, VocabularyDto vocabulary, TermDto term) {
    StringBuilder rqlQuery = new StringBuilder();
    if (table != null) {
      rqlQuery.append("in(project,(").append(table.getDatasourceName().replaceAll(" ", "+")).append(")),")
        .append("in(table,(").append(table.getName().replaceAll(" ", "+")).append(")),")
        .append("exists(name.analyzed),");
    }
    rqlQuery.append("in(").append(taxonomy.getName()).append("-").append(vocabulary.getName()).append(",").append(term.getName()).append(")");
    return rqlQuery.toString();
  }
}