package org.obiba.opal.web.magma;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import org.obiba.magma.ValueTable;

public abstract class CommonTable {

  private final ValueTable valueTable;

  private Set<Locale> locales;

  public CommonTable(ValueTable valueTable) {
    this.valueTable = valueTable;
  }

  public void setLocales(Set<Locale> locales) {
    this.locales = new LinkedHashSet<Locale>();
    if(locales != null) {
      this.locales.addAll(locales);
    }
  }

  ValueTable getValueTable() {
    return valueTable;
  }

  Set<Locale> getLocales() {
    return Collections.unmodifiableSet(locales);
  }
}
