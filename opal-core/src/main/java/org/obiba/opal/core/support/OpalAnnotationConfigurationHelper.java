package org.obiba.opal.core.support;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.magma.audit.hibernate.domain.HibernateVariableEntityAuditEvent;
import org.obiba.magma.audit.hibernate.domain.HibernateVariableEntityAuditLog;
import org.obiba.magma.datasource.hibernate.domain.CategoryState;
import org.obiba.magma.datasource.hibernate.domain.DatasourceState;
import org.obiba.magma.datasource.hibernate.domain.ValueSetState;
import org.obiba.magma.datasource.hibernate.domain.ValueSetValue;
import org.obiba.magma.datasource.hibernate.domain.ValueTableState;
import org.obiba.magma.datasource.hibernate.domain.VariableEntityState;
import org.obiba.magma.datasource.hibernate.domain.VariableState;
import org.obiba.magma.datasource.hibernate.domain.attribute.AttributeAwareAdapter;
import org.obiba.magma.datasource.hibernate.domain.attribute.HibernateAttribute;
import org.obiba.opal.core.crypt.StudyKeyStore;
import org.springframework.beans.factory.FactoryBean;

import com.google.common.collect.ImmutableSet;

public class OpalAnnotationConfigurationHelper implements FactoryBean {

  private ImmutableSet<Class<? extends AbstractEntity>> annotatedClasses;

  @SuppressWarnings("unchecked")
  public Object getObject() throws Exception {
    annotatedClasses = ImmutableSet.of(AttributeAwareAdapter.class, HibernateAttribute.class, StudyKeyStore.class, HibernateVariableEntityAuditEvent.class, HibernateVariableEntityAuditLog.class, CategoryState.class, DatasourceState.class, ValueSetState.class, ValueSetValue.class, ValueTableState.class, VariableEntityState.class, VariableState.class);
    return annotatedClasses;
  }

  public Class<?> getObjectType() {
    return annotatedClasses.getClass();
  }

  public boolean isSingleton() {
    return true;
  }

}
