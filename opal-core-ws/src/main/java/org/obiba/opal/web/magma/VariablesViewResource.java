package org.obiba.opal.web.magma;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.obiba.opal.web.model.Magma;

import edu.umd.cs.findbugs.annotations.Nullable;

public interface VariablesViewResource extends VariablesResource {

  @POST
  @Path("/file")
  Response addOrUpdateVariablesFromFile(Magma.ViewDto viewDto, @Nullable String comment);

}
