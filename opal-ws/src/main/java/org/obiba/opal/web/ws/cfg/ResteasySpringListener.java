package org.obiba.opal.web.ws.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.plugins.spring.SpringResourceFactory;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.PropertyInjector;
import org.jboss.resteasy.spi.Registry;
import org.jboss.resteasy.spi.ResourceFactory;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.util.GetRestful;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SmartApplicationListener;

/**
 * Original class copied from Yoga : https://github.com/sduskis/yoga (Apache License 2.0)
 * <p/>
 * Modified to support RESTEasy property injection (now implements BeanPostProcessor). This allows getting rid of the
 * SpringBeanPostProcessor completely.
 */
public class ResteasySpringListener implements SmartApplicationListener, BeanFactoryPostProcessor, BeanPostProcessor {

  private static final Logger log = LoggerFactory.getLogger(ResteasySpringListener.class);

  private final Registry registry;

  private final ResteasyProviderFactory providerFactory;

  private final Map<String, SpringResourceFactory> springResourceFactories
      = new HashMap<String, SpringResourceFactory>();

  private final List<String> providers = new ArrayList<String>();

  private ConfigurableListableBeanFactory beanFactory;

  public ResteasySpringListener(ResteasyProviderFactory providerFactory, Registry registry) {
    this.providerFactory = providerFactory;
    this.registry = registry;
  }

  @Override
  public void onApplicationEvent(ApplicationEvent event) {
    for(String provider : providers) {
      Object bean = beanFactory.getBean(provider);
      BeanDefinition beanDef = beanFactory.getBeanDefinition(provider);
      Class<?> beanClass = getBeanClass(beanDef);
      providerFactory.getInjectorFactory().createPropertyInjector(beanClass).inject(bean);
      providerFactory.registerProviderInstance(bean);
    }

    for(ResourceFactory resourceFactory : springResourceFactories.values()) {
      registry.addResourceFactory(resourceFactory);
    }
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    this.beanFactory = beanFactory;

    for(String name : beanFactory.getBeanDefinitionNames()) {

      BeanDefinition beanDef = beanFactory.getBeanDefinition(name);
      if(beanDef.getBeanClassName() == null || beanDef.isAbstract()) continue;

      Class<?> beanClass = getBeanClass(beanDef);

      if(beanClass.isAnnotationPresent(Provider.class)) {
        if(beanDef.isSingleton()) {
          providers.add(name);
        } else {
          log.warn("Classes annotated with @Provider must be singletons");
        }
      }

      if(GetRestful.isRootResource(beanClass)) {
        // Make resources lazy-init. This allows processing @Provider instances before resources.
        if(beanDef.isSingleton()) beanDef.setLazyInit(true);

        // defer registrations of resource factories until after all of the @Providers are registered
        springResourceFactories.put(name, new SpringResourceFactory(name, beanFactory, beanClass));
      }
    }
  }

  public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    return bean;
  }

  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    SpringResourceFactory resourceFactory = springResourceFactories.get(beanName);
    if(resourceFactory == null) return bean;

    BeanDefinition beanDef = beanFactory.getBeanDefinition(beanName);
    Class<?> beanClass = getBeanClass(beanDef);

    PropertyInjector propertyInjector = resourceFactory.getPropertyInjector();
    if(propertyInjector == null) {
      propertyInjector = providerFactory.getInjectorFactory().createPropertyInjector(beanClass);
    }

    HttpRequest request = ResteasyProviderFactory.getContextData(HttpRequest.class);
    if(beanDef.isSingleton() || request == null) {
      propertyInjector.inject(bean);
    } else {
      HttpResponse response = ResteasyProviderFactory.getContextData(HttpResponse.class);
      propertyInjector.inject(request, response, bean);
    }

    return bean;
  }

  protected Class<?> getBeanClass(BeanDefinition beanDef) {
    try {
      return Thread.currentThread().getContextClassLoader().loadClass(beanDef.getBeanClassName());
    } catch(ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public int getOrder() {
    return 1000;
  }

  @Override
  public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
    return eventType == ContextRefreshedEvent.class;
  }

  @Override
  public boolean supportsSourceType(Class<?> sourceType) {
    return ApplicationContext.class.isAssignableFrom(sourceType);
  }

}