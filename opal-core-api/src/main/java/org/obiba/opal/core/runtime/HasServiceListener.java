package org.obiba.opal.core.runtime;


public interface HasServiceListener<S extends Service> {

  void addListener(ServiceListener<S> listener);

}
