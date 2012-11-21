/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.services;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.obiba.opal.core.runtime.NoSuchServiceException;
import org.springframework.stereotype.Component;

@Component
@Provider
public class NoSuchServiceExceptionMapper implements ExceptionMapper<NoSuchServiceException> {

  @Override
  public Response toResponse(NoSuchServiceException exception) {
    return Response.status(Status.NOT_FOUND).entity(exception.getMessage()).build();
  }

}
