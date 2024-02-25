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

import java.util.Set;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.spi.DefaultOptionsMethodException;
import org.obiba.opal.web.ws.inject.RequestAttributesProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableSet;

@Provider
@Component
public class DefaultOptionsMethodExceptionMapper implements ExceptionMapper<DefaultOptionsMethodException> {

  private static final String ALLOW_HTTP_HEADER = "Allow";

  @Autowired
  private RequestAttributesProvider requestAttributeProvider;

  @Override
  public Response toResponse(DefaultOptionsMethodException exception) {
    Response response = exception.getResponse();
    // Extract the Allow header generated by RestEASY.
    // This contains all the methods of the resource class for the given path
    String availableMethods = (String) response.getMetadata().getFirst(ALLOW_HTTP_HEADER);
    UriInfo uri = requestAttributeProvider.getUriInfo();
    Set<String> allowed = AuthorizationInterceptor
        .allowed(uri.getPath(), ImmutableSet.copyOf(availableMethods.split(", ")));
    return Response.ok().header(ALLOW_HTTP_HEADER, AuthorizationInterceptor.asHeader(allowed)).build();
  }

}
