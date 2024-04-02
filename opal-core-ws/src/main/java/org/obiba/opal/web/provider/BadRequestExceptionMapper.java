/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.provider;

import org.obiba.opal.web.model.Ws;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.model.Ws;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;

@Component
@Provider
public class BadRequestExceptionMapper extends ErrorDtoExceptionMapper<BadRequestException> {

  private static final Logger log = LoggerFactory.getLogger(BadRequestExceptionMapper.class);

  @Override
  protected Response.Status getStatus() {
    return BAD_REQUEST;
  }

  @Override
  protected Ws.ClientErrorDto getErrorDto(BadRequestException exception) {
    log.warn("Bad request exception", exception);
    return ClientErrorDtos.getErrorMessage(getStatus(), "IllegalArgument", exception);
  }

}
