package org.obiba.opal.datasource;

import java.util.Date;

import org.obiba.opal.core.domain.data.Entity;
import org.obiba.opal.core.domain.metadata.Catalogue;

public interface DatasourceService {

  public String getName();

  public Entity fetchEntity(String entityId);

  public Catalogue loadCatalogue(String name);

  public boolean hasDataset(Entity entity, Catalogue catalogue, Date extractionDate);

}
