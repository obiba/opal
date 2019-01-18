package org.obiba.opal.core.event;

import org.obiba.magma.Datasource;

public class DatasourceDeletedEvent {

  private final Datasource datasource;

  public DatasourceDeletedEvent(Datasource datasource) {
    this.datasource = datasource;
  }

  public Datasource getDatasource() {
    return datasource;
  }
}
