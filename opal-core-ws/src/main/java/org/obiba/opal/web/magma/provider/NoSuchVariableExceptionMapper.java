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
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.obiba.magma.NoSuchVariableException;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.model.Ws;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Component
@Provider
public class NoSuchVariableExceptionMapper implements ExceptionMapper<NoSuchVariableException> {

  @Override
  public Response toResponse(NoSuchVariableException exception) {
    Ws.ClientErrorDto errorDto = Strings.isNullOrEmpty(exception.getValueTableName())
        ? ClientErrorDtos.getErrorMessage(NOT_FOUND, "NoSuchVariable").addArguments(exception.getName()).build()
        : ClientErrorDtos.getErrorMessage(NOT_FOUND, "NoSuchVariableInTable").addArguments(exception.getName())
            .addArguments(exception.getValueTableName()).build();
    return Response.status(NOT_FOUND).entity(errorDto).type("application/x-protobuf+json").build();
  }

}
