/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma.provider;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.mozilla.javascript.RhinoException;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.model.Ws.ClientErrorDto;
import org.springframework.stereotype.Component;

@Component
@Provider
public class JavaScriptExceptionMapper implements ExceptionMapper<RhinoException> {
  //
  // ExceptionMapper Methods
  //

  public Response toResponse(RhinoException exception) {
    ClientErrorDto.Builder errorDtoBuilder = ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "JavaScriptException", exception);
    return Response.status(Status.BAD_REQUEST).entity(errorDtoBuilder.build()).build();
  }
}
