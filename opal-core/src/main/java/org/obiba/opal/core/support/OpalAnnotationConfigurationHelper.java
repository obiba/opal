package org.obiba.opal.core.support;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.obiba.opal.core.domain.database.MongoDbDatabase;
import org.obiba.opal.core.domain.database.SqlDatabase;
import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.domain.unit.UnitKeyStoreState;
import org.obiba.opal.core.domain.user.Group;
import org.obiba.opal.core.domain.user.User;
import org.springframework.beans.factory.FactoryBean;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class OpalAnnotationConfigurationHelper implements FactoryBean<Set<Class<?>>> {

  private final Set<Class<?>> annotatedClasses = new ImmutableSet.Builder<Class<?>>()
      .add(UnitKeyStoreState.class, SubjectAcl.class, User.class, Group.class, SqlDatabase.class, MongoDbDatabase.class)
      .build();

  private final List<Class<?>> additionalClasses = Lists.newArrayList();

  @Override
  public Set<Class<?>> getObject() throws Exception {
    return ImmutableSet.copyOf(Iterables.concat(annotatedClasses, additionalClasses));
  }

  @Override
  public Class<?> getObjectType() {
    return Set.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

  public void setAdditionalClasses(Iterable<?> additionalClasses) {
    for(Collection<?> c : Iterables.filter(additionalClasses, Collection.class)) {
      setAdditionalClasses(c);
    }
    for(Class<?> c : Iterables.filter(additionalClasses, Class.class)) {
      this.additionalClasses.add(c);
    }
  }

}
