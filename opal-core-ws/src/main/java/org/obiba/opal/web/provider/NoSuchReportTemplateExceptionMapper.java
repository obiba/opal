/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.provider;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.obiba.opal.core.service.NoSuchReportTemplateException;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.springframework.stereotype.Component;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Component
@Provider
public class NoSuchReportTemplateExceptionMapper implements ExceptionMapper<NoSuchReportTemplateException> {

  @Override
  public Response toResponse(NoSuchReportTemplateException exception) {
    return Response.status(NOT_FOUND).entity(
        ClientErrorDtos.getErrorMessage(NOT_FOUND, "NoSuchReportTemplate").addArguments(exception.getName())
            .addArguments(exception.getProject()).build()).build();
  }
}
