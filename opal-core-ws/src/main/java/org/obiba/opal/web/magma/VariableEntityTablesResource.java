package org.obiba.opal.web.magma;

import java.util.List;

import javax.ws.rs.GET;

import org.obiba.magma.support.VariableEntityBean;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.ws.security.NoAuthorization;

public interface VariableEntityTablesResource {

  void setVariableEntity(VariableEntityBean variableEntity);

  @GET
  @NoAuthorization
  List<Magma.TableDto> getTables();

  List<Magma.TableDto> getTables(int limit);
}
