package org.obiba.opal.web.magma;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;

abstract class AbstractValueTableResource {

  private final ValueTable valueTable;

  private final Set<Locale> locales;

  public AbstractValueTableResource(ValueTable valueTable, Set<Locale> locales) {
    this.valueTable = valueTable;
    this.locales = new LinkedHashSet<Locale>();
    this.locales.addAll(locales);
  }

  ValueTable getValueTable() {
    return valueTable;
  }

  Datasource getDatasource() {
    return valueTable.getDatasource();
  }

  Set<Locale> getLocales() {
    return Collections.unmodifiableSet(locales);
  }
}
