package org.obiba.opal.core.support;

import org.obiba.opal.core.domain.participant.Participant;
import org.obiba.opal.core.domain.participant.ParticipantKey;
import org.springframework.beans.factory.FactoryBean;

import com.google.common.collect.ImmutableSet;

public class KeyAnnotationConfigurationHelper implements FactoryBean {

  private ImmutableSet<Class<?>> annotatedClasses;

  public Object getObject() throws Exception {
    annotatedClasses = ImmutableSet.of(Participant.class, ParticipantKey.class);
    return annotatedClasses;
  }

  public Class<?> getObjectType() {
    return annotatedClasses.getClass();
  }

  public boolean isSingleton() {
    return true;
  }

}
