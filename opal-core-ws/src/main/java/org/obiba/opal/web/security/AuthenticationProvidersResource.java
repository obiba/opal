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

import com.google.common.base.Strings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.obiba.oidc.OIDCConfigurationProvider;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.security.Dtos;
import org.obiba.opal.web.ws.security.NotAuthenticated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Path("/auth/providers")
@Tag(name = "Authentication", description = "Operations related to user authentication")
public class AuthenticationProvidersResource {

  private final OIDCConfigurationProvider authConfigurationProvider;

  @Autowired
  public AuthenticationProvidersResource(OIDCConfigurationProvider authConfigurationProvider) {
    this.authConfigurationProvider = authConfigurationProvider;
  }

  @GET
  @NotAuthenticated
  @Operation(
    summary = "List authentication providers",
    description = "Retrieves all available authentication providers configured in the system. Filters providers based on the current request's entry point to ensure compatibility with the callback URLs."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Authentication providers successfully retrieved"),
    @ApiResponse(responseCode = "500", description = "Error retrieving authentication provider configurations")
  })
  public List<Opal.AuthProviderDto> list(@Context HttpServletRequest request) {
    String entryPoint = request.getScheme() + "://" + request.getHeader("Host");
    return authConfigurationProvider.getConfigurations().stream()
        .filter(provider -> {
          if (Strings.isNullOrEmpty(entryPoint)) return true;
          if (!provider.hasCallbackURL()) return true;
          return provider.getCallbackURL().startsWith(entryPoint);
        })
        .map(Dtos::asSummaryDto)
        .collect(Collectors.toList());
  }
}
