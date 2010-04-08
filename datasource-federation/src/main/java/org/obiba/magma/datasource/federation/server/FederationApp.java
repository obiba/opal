package org.obiba.magma.datasource.federation.server;

import java.util.Set;

import javax.ws.rs.core.Application;

import com.google.common.collect.ImmutableSet;

public class FederationApp extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    return ImmutableSet.<Class<?>> builder().add(RestDatasource.class).build();
  }

}
