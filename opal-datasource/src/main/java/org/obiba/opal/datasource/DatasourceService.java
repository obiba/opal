package org.obiba.opal.datasource;

import java.util.Date;

import org.obiba.opal.core.domain.data.Entity;
import org.obiba.opal.core.domain.metadata.Catalogue;

public interface DatasourceService {

  public String getName();
  
  public String getType();

  public Entity fetchEntity(String entityId);

  public void registerKey(String entityId, String owner, String ownerKey);
  
  public Catalogue loadCatalogue(String name) throws InvalidCatalogueException;

  public boolean hasDataset(Entity entity, Catalogue catalogue, Date extractionDate);

}
