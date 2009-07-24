package org.obiba.opal.datasource.support;

import java.util.List;

import org.hibernate.SessionFactory;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.opal.core.domain.data.Entity;
import org.obiba.opal.core.service.IOpalKeyRegistry;
import org.obiba.opal.datasource.EntityProvider;
import org.springframework.beans.factory.InitializingBean;

public class DefaultEntityProvider implements EntityProvider, InitializingBean {

  private IOpalKeyRegistry opalKeyRegistry;

  private SessionFactory sessionFactory;

  private String entityType;

  public void setOpalKeyRegistry(IOpalKeyRegistry opalKeyRegistry) {
    this.opalKeyRegistry = opalKeyRegistry;
  }

  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public void afterPropertiesSet() throws Exception {
    if(opalKeyRegistry == null) throw new IllegalStateException("opalKeyRegistry is required");
    if(sessionFactory == null) throw new IllegalStateException("sessionFactory is required");
    if(entityType == null) throw new IllegalStateException("entityType is required");
  }

  public Entity fetchEntity(String datasource, String entityId) {
    String opalKey = opalKeyRegistry.findOpalKey(datasource, entityId);
    if(opalKey == null) {
      opalKey = opalKeyRegistry.registerNewOpalKey(datasource, entityId);
    }

    Entity entity = doLookupEntity(opalKey);
    if(entity == null) {
      entity = new Entity(entityType, opalKey);
    }
    return entity;
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
