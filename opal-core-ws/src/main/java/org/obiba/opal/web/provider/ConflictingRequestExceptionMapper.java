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

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.Provider;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.model.Ws;
import org.obiba.opal.web.support.ConflictingRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Provider
public class ConflictingRequestExceptionMapper extends ErrorDtoExceptionMapper<ConflictingRequestException> {
  private static final Logger log = LoggerFactory.getLogger(ConflictingRequestExceptionMapper.class);

  @Override
  protected Response.Status getStatus() {
    return Status.CONFLICT;
  }


  @Override
  protected Ws.ClientErrorDto getErrorDto(ConflictingRequestException exception) {
    log.warn("Conflict exception", exception);
    return ClientErrorDtos.getErrorMessage(getStatus(), "Conflict", exception);
  }
}
