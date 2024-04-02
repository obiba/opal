/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.security;

import org.obiba.opal.web.model.Ws;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.model.Ws;
import org.obiba.opal.web.provider.ErrorDtoExceptionMapper;
import org.obiba.shiro.web.filter.UserBannedException;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import static jakarta.ws.rs.core.Response.Status.FORBIDDEN;

@Provider
@Component
public class UserBannedExceptionMapper extends ErrorDtoExceptionMapper<UserBannedException> {

  @Override
  protected Response.Status getStatus() {
    return FORBIDDEN;
  }

  @Override
  protected Ws.ClientErrorDto getErrorDto(UserBannedException exception) {
    Ws.ClientErrorDto.Builder builder = ClientErrorDtos.getErrorMessage(getStatus(), "BannedUser", exception).toBuilder();
    return builder.clearArguments().addArguments(exception.getUser()).addArguments(exception.getRemainingBanTime() + "").build();
  }

}
