/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.system.subject;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.obiba.opal.core.service.SubjectProfileNotFoundException;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.springframework.stereotype.Component;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Component
@Provider
public class SubjectProfileNotFoundExceptionMapper implements ExceptionMapper<SubjectProfileNotFoundException> {

  @Override
  public Response toResponse(SubjectProfileNotFoundException exception) {
    return Response.status(NOT_FOUND)
        .entity(ClientErrorDtos.getErrorMessage(NOT_FOUND, "SubjectProfileNotFound", exception.getPrincipal())).build();
  }

}
