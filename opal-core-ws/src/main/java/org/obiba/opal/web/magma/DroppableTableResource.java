package org.obiba.opal.web.magma;

import javax.ws.rs.DELETE;
import javax.ws.rs.core.Response;

public interface DroppableTableResource extends TableResource {

  @DELETE
  Response drop();
}
