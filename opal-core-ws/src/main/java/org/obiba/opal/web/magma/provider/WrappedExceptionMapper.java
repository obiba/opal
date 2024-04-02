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

import org.mozilla.javascript.WrappedException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.provider.ErrorDtoExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import org.obiba.opal.web.model.Ws;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

@Component
@Provider
public class WrappedExceptionMapper extends ErrorDtoExceptionMapper<WrappedException> {

  private static final Logger log = LoggerFactory.getLogger(WrappedExceptionMapper.class);

  @Override
  protected Response.Status getStatus() {
    return BAD_REQUEST;
  }

  @Override
  protected Ws.ClientErrorDto getErrorDto(WrappedException exception) {
    if(exception.getWrappedException() instanceof NoSuchVariableException) {
      return getErrorDto((NoSuchVariableException) exception.getWrappedException());
    } else {
      return ClientErrorDtos.getErrorMessage(getStatus(), "JavaScriptException", exception);
    }
  }

  protected Ws.ClientErrorDto getErrorDto(NoSuchVariableException exception) {
    return (Strings.isNullOrEmpty(exception.getValueTableName())
        ? ClientErrorDtos.getErrorMessage(getStatus(), "NoSuchVariable").addArguments(exception.getName())
        : ClientErrorDtos.getErrorMessage(getStatus(), "NoSuchVariableInTable").addArguments(exception.getName())
            .addArguments(exception.getValueTableName())).build();
  }

}
