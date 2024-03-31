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

import com.google.protobuf.GeneratedMessage;
import org.apache.shiro.authz.UnauthorizedException;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;

@Component
@Provider
public class UnauthorizedExceptionMapper extends ErrorDtoExceptionMapper<UnauthorizedException> {

  private static final Logger log = LoggerFactory.getLogger(UnauthorizedExceptionMapper.class);

  @Override
  protected Response.Status getStatus() {
    return FORBIDDEN;
  }

  @Override
  protected GeneratedMessage.ExtendableMessage<?> getErrorDto(UnauthorizedException exception) {
    if (log.isDebugEnabled())
      log.warn("Unauthorized exception", exception);
    return ClientErrorDtos.getErrorMessage(getStatus(), "Forbidden", exception);
  }

}
