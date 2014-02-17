/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.magma.provider;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.obiba.magma.DuplicateDatasourceNameException;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.springframework.stereotype.Component;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Component
@Provider
public class DuplicateDatasourceNameExceptionMapper implements ExceptionMapper<DuplicateDatasourceNameException> {

  @Override
  public Response toResponse(DuplicateDatasourceNameException exception) {
    return Response.status(BAD_REQUEST)
        .entity(ClientErrorDtos.getErrorMessage(BAD_REQUEST, "DuplicateDatasourceName").build()).build();
  }
}
