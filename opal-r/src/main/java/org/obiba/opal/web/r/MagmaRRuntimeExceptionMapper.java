/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.r;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.obiba.opal.r.MagmaRRuntimeException;
import org.springframework.stereotype.Component;

@Component
@Provider
public class MagmaRRuntimeExceptionMapper implements ExceptionMapper<MagmaRRuntimeException> {

  @Override
  public Response toResponse(MagmaRRuntimeException exception) {
    String message = exception.getMessage();
    if(exception.getCause() != null && exception.getCause().getMessage() != null) {
      message = message + ": " + exception.getCause().getMessage();
    }
    return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(message).build();
  }

}
