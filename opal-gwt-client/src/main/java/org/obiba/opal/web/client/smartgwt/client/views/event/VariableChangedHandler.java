package org.obiba.opal.web.client.smartgwt.client.views.event;

import com.google.gwt.event.shared.EventHandler;

public interface VariableChangedHandler extends EventHandler {
  void onVariableChanged(VariableChangedEvent event);
}