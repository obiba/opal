package org.obiba.opal.core.service;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Allow static access to ApplicationContext.
 * But be sure it is initialized before you access it!!
 */
@Component
public class ApplicationContextProvider implements ApplicationContextAware {

  @SuppressWarnings("StaticNonFinalField")
  private static ApplicationContext applicationContext;

  public static ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  @Override
  @SuppressWarnings({ "AccessStaticViaInstance", "AssignmentToStaticFieldFromInstanceMethod" })
  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }
}
