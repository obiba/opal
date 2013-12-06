package org.obiba.opal.core.runtime;

/**
 * A service listener is called when service state changes, allowing to perform clean-up operations.
 */
public interface ServiceListener<S extends Service> {

  /**
   * Called after service is started.
   * @param service
   */
  void onServiceStart(S service);

  /**
   * Called before service is stopped.
   * @param service
   */
  void onServiceStop(S service);

}
