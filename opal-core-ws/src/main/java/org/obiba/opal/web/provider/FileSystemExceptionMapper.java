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

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.apache.commons.vfs2.FileSystemException;
import org.springframework.stereotype.Component;

@Component
@Provider
public class FileSystemExceptionMapper implements ExceptionMapper<FileSystemException> {

  @Override
  public Response toResponse(FileSystemException exception) {
    return Response.status(Status.NOT_FOUND).entity(exception.getMessage()).build();
  }

}
