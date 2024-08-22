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
import org.obiba.opal.search.service.SearchException;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Component
@Provider
public class SearchExceptionMapper extends ErrorDtoExceptionMapper<SearchException> {

  @Override
  protected Response.Status getStatus() {
    return Response.Status.INTERNAL_SERVER_ERROR;
  }

  @Override
  protected Ws.ClientErrorDto getErrorDto(SearchException exception) {
    return ClientErrorDtos.getErrorMessage(getStatus(), "SearchFailure").addArguments(exception.getMessage()).build();
  }

}
