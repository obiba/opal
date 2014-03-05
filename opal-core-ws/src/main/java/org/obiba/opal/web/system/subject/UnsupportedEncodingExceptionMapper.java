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

import java.io.UnsupportedEncodingException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.obiba.opal.core.service.OpalGeneralConfigService;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.model.Ws;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Component
@Provider
public class UnsupportedEncodingExceptionMapper implements ExceptionMapper<UnsupportedEncodingException> {

  @Autowired
  private OpalGeneralConfigService opalGeneralConfigService;

  @Override
  public Response toResponse(UnsupportedEncodingException exception) {
    Ws.ClientErrorDto errorDto = ClientErrorDtos.getErrorMessage(BAD_REQUEST, "UnsupportedEncoding")
        .addArguments(opalGeneralConfigService.getConfig().getDefaultCharacterSet()).build();
    return Response.status(BAD_REQUEST).entity(errorDto).type("application/x-protobuf+json").build();
  }

}
