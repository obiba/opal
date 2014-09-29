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
import javax.ws.rs.ext.Provider;

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.provider.ErrorDtoExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.protobuf.GeneratedMessage;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Component
@Provider
public class MagmaRuntimeExceptionMapper extends ErrorDtoExceptionMapper<MagmaRuntimeException> {

  private static final Logger log = LoggerFactory.getLogger(MagmaRuntimeExceptionMapper.class);

  @Override
  protected Response.Status getStatus() {
    return BAD_REQUEST;
  }

  @Override
  protected GeneratedMessage.ExtendableMessage<?> getErrorDto(MagmaRuntimeException exception) {
    log.debug("Magma exception", exception);
    return ClientErrorDtos.getErrorMessage(getStatus(), "MagmaRuntimeException", exception.getMessage()).build();
  }

}
