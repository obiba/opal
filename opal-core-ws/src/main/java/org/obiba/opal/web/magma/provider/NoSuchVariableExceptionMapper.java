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

import org.obiba.magma.NoSuchVariableException;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.provider.ErrorDtoExceptionMapper;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.google.protobuf.GeneratedMessage;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Component
@Provider
public class NoSuchVariableExceptionMapper extends ErrorDtoExceptionMapper<NoSuchVariableException> {

  @Override
  protected Response.Status getStatus() {
    return NOT_FOUND;
  }

  @Override
  protected GeneratedMessage.ExtendableMessage<?> getErrorDto(NoSuchVariableException exception) {
    return (Strings.isNullOrEmpty(exception.getValueTableName())
        ? ClientErrorDtos.getErrorMessage(getStatus(), "NoSuchVariable").addArguments(exception.getName())
        : ClientErrorDtos.getErrorMessage(getStatus(), "NoSuchVariableInTable").addArguments(exception.getName())
            .addArguments(exception.getValueTableName())).build();
  }

}
