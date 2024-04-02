/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.magma.provider;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.magma.support.NoSuchDatasourceFactoryException;
import org.obiba.opal.web.provider.ErrorDtoExceptionMapper;
import org.springframework.stereotype.Component;

import org.obiba.opal.web.model.Ws;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;

@Component
@Provider
public class NoSuchDatasourceFactoryExceptionMapper extends ErrorDtoExceptionMapper<NoSuchDatasourceFactoryException> {

  @Override
  protected Response.Status getStatus() {
    return BAD_REQUEST;
  }

  @Override
  protected Ws.ClientErrorDto getErrorDto(NoSuchDatasourceFactoryException exception) {
    return ClientErrorDtos.getErrorMessage(getStatus(), "UnidentifiedDatasourceFactory").build();
  }

}
