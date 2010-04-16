/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.server.rest.jaxrs.provider;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.obiba.magma.NoSuchVariableException;
import org.springframework.stereotype.Component;

@Component
@Provider
public class NoSuchVariableExceptionMapper implements ExceptionMapper<NoSuchVariableException> {

  @Override
  public Response toResponse(NoSuchVariableException exception) {
    return Response.status(Status.NOT_FOUND).build();
  }

}
