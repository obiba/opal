package org.obiba.opal.datasource.impl;

import java.util.Date;
import java.util.List;

import org.hibernate.SessionFactory;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.opal.core.domain.data.Dataset;
import org.obiba.opal.core.domain.data.Entity;
import org.obiba.opal.core.domain.metadata.Catalogue;
import org.obiba.opal.core.service.IOpalKeyRegistry;
import org.obiba.opal.core.service.MetaDataService;
import org.obiba.opal.datasource.DatasourceService;
import org.obiba.opal.datasource.InvalidCatalogueException;
import org.springframework.beans.factory.InitializingBean;

public class DefaultDatasourceServiceImpl implements DatasourceService, InitializingBean {
      
  private String datasourceName;

  private String datasourceType;

  /** Type of entity created by this datasource */
  private String entityType;

  private IOpalKeyRegistry opalKeyRegistry;

  private SessionFactory sessionFactory;

  private MetaDataService metaDataService;

  public void setOpalKeyRegistry(IOpalKeyRegistry opalKeyRegistry) {
    this.opalKeyRegistry = opalKeyRegistry;
  }

  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public void setDatasourceName(String datasourceName) {
    this.datasourceName = datasourceName;
  }

  public void setDatasourceType(String datasourceType) {
    this.datasourceType = datasourceType;
  }

  public void setMetaDataService(MetaDataService metaDataService) {
    this.metaDataService = metaDataService;
  }

  public void afterPropertiesSet() throws Exception {
    if(opalKeyRegistry == null) throw new IllegalStateException("opalKeyRegistry is required");
    if(entityType == null) throw new IllegalStateException("entityType is required");
    if(datasourceName == null) throw new IllegalStateException("datasourceName is required");
    if(datasourceType == null) throw new IllegalStateException("datasourceType is required");
    if(metaDataService == null) throw new IllegalStateException("metaDataService is required");
  }

  public String getName() {
    return datasourceName;
  }

  public String getType() {
    return datasourceType;
  }

  public boolean hasDataset(Entity entity, Catalogue catalogue, Date extractionDate) {
    // Don't use template-based comparision due to Dataset.creationDate; which is set in Dataset ctor.
    int count = AssociationCriteria.create(Dataset.class, sessionFactory.getCurrentSession()).add("entity", Operation.match, entity).add("catalogue", Operation.eq, catalogue).add("extractionDate", Operation.eq, extractionDate).count();
    return count > 0;
  }

  public Catalogue loadCatalogue(String name) {
    Catalogue catalogue = metaDataService.getCatalogue(datasourceName, name);
    if(catalogue == null) {
      throw new InvalidCatalogueException(datasourceName, name);
    }
    return catalogue;
  }

  public Entity fetchEntity(String entityId) {
    String opalKey = opalKeyRegistry.findOpalKey(datasourceName, entityId);
    if(opalKey == null) {
      opalKey = opalKeyRegistry.registerNewOpalKey(datasourceName, entityId);
    }

    Entity entity = doLookupEntity(opalKey);
    if(entity == null) {
      entity = new Entity(entityType, opalKey);
    }
    return entity;
  }

  public void registerKey(String entityId, String owner, String ownerKey) {
    if (!opalKeyRegistry.hasOpalKey(owner, ownerKey)) {
      opalKeyRegistry.registerKey(entityId, owner, ownerKey);
    }
  }
  
  /**
   * Lookup an existing entity in order to re-use it.
   * @param type
   * @param identifier
   * @return
   */
  protected Entity doLookupEntity(String identifier) {
    List<Entity> result = AssociationCriteria.create(Entity.class, sessionFactory.getCurrentSession()).add("type", Operation.eq, entityType).add("identifier", Operation.eq, identifier).list();
    if(result.size() > 0) {
      return result.get(0);
    }
    return null;

  }

}
