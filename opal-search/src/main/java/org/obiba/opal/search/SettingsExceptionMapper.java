/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.search;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.elasticsearch.common.settings.SettingsException;
import org.obiba.opal.web.model.Ws;
import org.springframework.stereotype.Component;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Component
@Provider
public class SettingsExceptionMapper implements ExceptionMapper<SettingsException> {

  @Override
  public Response toResponse(SettingsException exception) {
    return Response.status(BAD_REQUEST).type("application/x-protobuf+json").entity(
        Ws.ClientErrorDto.newBuilder().setCode(BAD_REQUEST.getStatusCode()).setStatus(exception.getMessage()).build())
        .build();
  }

}
