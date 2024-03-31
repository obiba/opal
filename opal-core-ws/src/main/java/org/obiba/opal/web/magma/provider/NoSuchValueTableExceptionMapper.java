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

import org.obiba.magma.NoSuchValueTableException;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.provider.ErrorDtoExceptionMapper;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import org.obiba.opal.web.model.Ws;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

@Component
@Provider
public class NoSuchValueTableExceptionMapper extends ErrorDtoExceptionMapper<NoSuchValueTableException> {

  @Override
  protected Response.Status getStatus() {
    return NOT_FOUND;
  }

  @Override
  protected Ws.ClientErrorDto getErrorDto(NoSuchValueTableException exception) {
    return (Strings.isNullOrEmpty(exception.getDatasourceName())
        ? ClientErrorDtos.getErrorMessage(getStatus(), "NoSuchValueTable").addArguments(exception.getTableName())
        : ClientErrorDtos.getErrorMessage(getStatus(), "NoSuchValueTableInDatasource")
            .addArguments(exception.getTableName()).addArguments(exception.getDatasourceName())).build();
  }

}
