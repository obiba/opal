/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.datashield;

import org.obiba.opal.web.model.Ws;
import org.obiba.datashield.core.NoSuchDSMethodException;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.provider.ErrorDtoExceptionMapper;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.Provider;

@Component
@Provider
public class NoSuchDSMethodExceptionMapper extends ErrorDtoExceptionMapper<NoSuchDSMethodException> {

  @Override
  protected Status getStatus() {
    return Status.NOT_FOUND;
  }

  @Override
  protected Ws.ClientErrorDto getErrorDto(NoSuchDSMethodException exception) {
    return ClientErrorDtos.getErrorMessage(getStatus(), "DataShieldMethodError", exception);
  }

}
