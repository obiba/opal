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
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.obiba.opal.web.UnsatisfiedPreconditionException;
import org.springframework.stereotype.Component;

@Component
@Provider
public class UnsatisfiedPreconditionExceptionMapper implements ExceptionMapper<UnsatisfiedPreconditionException> {

  @Override
  public Response toResponse(UnsatisfiedPreconditionException exception) {
    return exception.getResponse().build();
  }

}
