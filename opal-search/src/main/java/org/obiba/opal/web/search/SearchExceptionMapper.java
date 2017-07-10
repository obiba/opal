/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.search;

import org.obiba.opal.spi.search.SearchException;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Component
@Provider
public class SearchExceptionMapper implements ExceptionMapper<SearchException> {

  @Override
  public Response toResponse(SearchException exception) {
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(exception.getMessage()).build();
  }

}
