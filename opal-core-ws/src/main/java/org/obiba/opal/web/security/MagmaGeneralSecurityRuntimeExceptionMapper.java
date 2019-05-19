/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.security;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.obiba.opal.core.security.GeneralSecurityRuntimeException;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.provider.ErrorDtoExceptionMapper;
import org.springframework.stereotype.Component;

import com.google.protobuf.GeneratedMessage;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Provider
@Component
public class MagmaGeneralSecurityRuntimeExceptionMapper
    extends ErrorDtoExceptionMapper<GeneralSecurityRuntimeException> {

  @Override
  protected Response.Status getStatus() {
    return BAD_REQUEST;
  }

  @Override
  protected GeneratedMessage.ExtendableMessage<?> getErrorDto(GeneralSecurityRuntimeException exception) {
    return ClientErrorDtos.getErrorMessage(getStatus(), "InvalidKeypair", exception);
  }

}
