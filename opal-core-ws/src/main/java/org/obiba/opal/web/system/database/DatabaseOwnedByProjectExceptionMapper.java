/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.system.database;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import org.obiba.opal.core.service.database.DatabaseOwnedByProjectException;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.provider.ErrorDtoExceptionMapper;
import org.springframework.stereotype.Component;

import org.obiba.opal.web.model.Ws;

import static jakarta.ws.rs.core.Response.Status.CONFLICT;

/**
 * A conflict rather than a forbidden: the request is refused because of the state of the server - a project owns this
 * database - and it succeeds once that project is gone.
 */
@Component
@Provider
public class DatabaseOwnedByProjectExceptionMapper extends ErrorDtoExceptionMapper<DatabaseOwnedByProjectException> {

  @Override
  protected Response.Status getStatus() {
    return CONFLICT;
  }

  @Override
  protected Ws.ClientErrorDto getErrorDto(DatabaseOwnedByProjectException exception) {
    return ClientErrorDtos.getErrorMessage(getStatus(), "DatabaseOwnedByProject", exception);
  }

}
