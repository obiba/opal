package org.obiba.opal.core.support;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.domain.unit.UnitKeyStoreState;
import org.springframework.beans.factory.FactoryBean;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class OpalAnnotationConfigurationHelper implements FactoryBean {
  private final Set<Class<?>> annotatedClasses = new ImmutableSet.Builder<Class<?>>().add(UnitKeyStoreState.class, SubjectAcl.class).build();

  private final List<Class<?>> additionalClasses = Lists.newArrayList();

  public Object getObject() throws Exception {
    return ImmutableSet.copyOf(Iterables.concat(annotatedClasses, additionalClasses));
  }

  public Class<?> getObjectType() {
    return Set.class;
  }

  public boolean isSingleton() {
    return true;
  }

  public void setAdditionalClasses(Collection<?> additionalClasses) {
    for(Collection<?> c : Iterables.filter(additionalClasses, Collection.class)) {
      setAdditionalClasses(c);
    }
    for(Class<?> c : Iterables.filter(additionalClasses, Class.class)) {
      this.additionalClasses.add(c);
    }
  }

}
