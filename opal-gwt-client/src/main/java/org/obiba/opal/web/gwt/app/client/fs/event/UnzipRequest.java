package org.obiba.opal.web.gwt.app.client.fs.event;

import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.dispatch.annotation.Optional;
import com.gwtplatform.dispatch.annotation.Order;

@GenEvent
public class UnzipRequest {
  @Order(0)
  String archive;

  @Order(1)
  String destination;

  @Optional
  @Order(2)
  String password;
}
