/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
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

import org.obiba.opal.web.magma.ClientErrorDtos;
import org.springframework.stereotype.Component;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Component
@Provider
public class PasswordNotChangedExceptionMapper implements ExceptionMapper<PasswordNotChangedException> {
  @Override
  public Response toResponse(PasswordNotChangedException exception) {
    return Response.status(BAD_REQUEST).entity(
        ClientErrorDtos.getErrorMessage(BAD_REQUEST, "PasswordNotChanged", exception)).build();
  }
}

