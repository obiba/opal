/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.project;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.obiba.opal.core.service.NoSuchProjectException;
import org.springframework.stereotype.Component;

@Component
@Provider
public class NoSuchProjectExceptionMapper implements ExceptionMapper<NoSuchProjectException> {

  @Override
  public Response toResponse(NoSuchProjectException exception) {
    return Response.status(Response.Status.NOT_FOUND).entity(exception.getMessage()).build();
  }
}
