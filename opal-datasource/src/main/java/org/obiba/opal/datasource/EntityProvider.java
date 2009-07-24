package org.obiba.opal.datasource;

import org.obiba.opal.core.domain.data.Entity;

public interface EntityProvider {

  public Entity fetchEntity(String datasource, String entityId);

}
