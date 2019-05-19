/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma.provider;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.provider.ErrorDtoExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.protobuf.GeneratedMessage;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

@Component
@Provider
public class UnhandledExceptionMapper extends ErrorDtoExceptionMapper<Exception> {

  private static final Logger log = LoggerFactory.getLogger(UnhandledExceptionMapper.class);

  @Override
  protected Response.Status getStatus() {
    return INTERNAL_SERVER_ERROR;
  }

  @Override
  protected GeneratedMessage.ExtendableMessage<?> getErrorDto(Exception exception) {
    log.error("Unhandled exception", exception);
    return ClientErrorDtos.getErrorMessage(getStatus(), "UnhandledException", exception.getClass().getSimpleName(),
        exception.getMessage()).build();
  }

}
