package org.obiba.opal.web.magma;

import java.util.Locale;
import java.util.Set;

import javax.ws.rs.PUT;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.obiba.opal.web.model.Magma;

import edu.umd.cs.findbugs.annotations.Nullable;

public interface VariableViewResource extends VariableResource {

  void setLocales(Set<Locale> locales);

  @PUT
  Response createOrUpdateVariable(Magma.VariableDto variable, @Nullable @QueryParam("comment") String comment);
}
