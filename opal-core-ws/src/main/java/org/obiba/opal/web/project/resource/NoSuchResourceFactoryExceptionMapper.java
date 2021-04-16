/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.project.resource;

import com.google.protobuf.GeneratedMessage;
import org.obiba.opal.core.service.NoSuchResourceFactoryException;
import org.obiba.opal.core.service.NoSuchResourceReferenceException;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.provider.ErrorDtoExceptionMapper;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Component
@Provider
public class NoSuchResourceFactoryExceptionMapper extends ErrorDtoExceptionMapper<NoSuchResourceFactoryException> {

  @Override
  protected Response.Status getStatus() {
    return NOT_FOUND;
  }

  @Override
  protected GeneratedMessage.ExtendableMessage<?> getErrorDto(NoSuchResourceFactoryException exception) {
    return ClientErrorDtos.getErrorMessage(getStatus(), "NoSuchResourceFactory")
        .addArguments(exception.getProvider()).addArguments(exception.getName())
        .build();
  }

}
