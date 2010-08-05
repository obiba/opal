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
import org.mozilla.javascript.WrappedException;
import org.springframework.stereotype.Component;

@Component
@Provider
public class RhinoExceptionMapper implements ExceptionMapper<RhinoException> {

  @Override
  public Response toResponse(RhinoException exception) {
    Throwable t = exception;
    if(exception instanceof WrappedException) {
      t = ((WrappedException) exception).getWrappedException();
    }
    return Response.status(Status.BAD_REQUEST).entity(t.getMessage()).build();
  }

}
