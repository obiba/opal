/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.system.subject;

import com.google.protobuf.GeneratedMessage;
import org.obiba.opal.core.service.security.PasswordTooLongException;
import org.obiba.opal.core.service.security.PasswordTooShortException;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.provider.ErrorDtoExceptionMapper;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Component
@Provider
public class PasswordTooLongExceptionMapper extends ErrorDtoExceptionMapper<PasswordTooLongException> {

  @Override
  protected Response.Status getStatus() {
    return BAD_REQUEST;
  }

  @Override
  protected GeneratedMessage.ExtendableMessage<?> getErrorDto(PasswordTooLongException exception) {
    return ClientErrorDtos.getErrorMessage(getStatus(), "PasswordLengthMax", exception);
  }

}

