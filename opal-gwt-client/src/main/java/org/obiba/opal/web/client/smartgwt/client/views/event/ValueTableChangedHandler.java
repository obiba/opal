package org.obiba.opal.web.client.smartgwt.client.views.event;

import com.google.gwt.event.shared.EventHandler;

public interface ValueTableChangedHandler extends EventHandler {
  void onValueTableChanged(ValueTableChangedEvent event);
}