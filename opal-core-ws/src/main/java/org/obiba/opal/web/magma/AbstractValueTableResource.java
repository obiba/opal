package org.obiba.opal.web.magma;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.js.views.JavascriptClause;

import com.google.common.collect.Lists;

abstract class AbstractValueTableResource {

  private final ValueTable valueTable;

  private final Set<Locale> locales;

  public AbstractValueTableResource(ValueTable valueTable, Set<Locale> locales) {
    this.valueTable = valueTable;
    this.locales = new LinkedHashSet<Locale>();
    this.locales.addAll(locales);
  }

  protected ValueTable getValueTable() {
    return valueTable;
  }

  protected Datasource getDatasource() {
    return valueTable.getDatasource();
  }

  protected Set<Locale> getLocales() {
    return Collections.unmodifiableSet(locales);
  }

  protected LocalesResource getLocalesResource() {
    return new LocalesResource(locales);
  }

  protected Iterable<Variable> filterVariables(String script, Integer offset, Integer limit) {
    List<Variable> filteredVariables = null;

    if(script != null) {
      JavascriptClause jsClause = new JavascriptClause(script);
      jsClause.initialise();

      filteredVariables = new ArrayList<Variable>();
      for(Variable variable : getValueTable().getVariables()) {
        if(jsClause.select(variable)) {
          filteredVariables.add(variable);
        }
      }
    } else {
      filteredVariables = Lists.newArrayList(getValueTable().getVariables());
    }

    int fromIndex = (offset < filteredVariables.size()) ? offset : filteredVariables.size();
    int toIndex = (limit != null) ? Math.min(fromIndex + limit, filteredVariables.size()) : filteredVariables.size();

    return filteredVariables.subList(fromIndex, toIndex);
  }
}
