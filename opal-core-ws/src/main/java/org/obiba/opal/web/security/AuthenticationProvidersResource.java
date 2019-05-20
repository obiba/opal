package org.obiba.opal.web.security;

import org.obiba.oidc.OIDCConfigurationProvider;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.ws.security.NotAuthenticated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Path("/auth/providers")
public class AuthenticationProvidersResource {

  private final OIDCConfigurationProvider authConfigurationProvider;

  @Autowired
  public AuthenticationProvidersResource(OIDCConfigurationProvider authConfigurationProvider) {
    this.authConfigurationProvider = authConfigurationProvider;
  }

  @GET
  @NotAuthenticated
  public List<Opal.AuthProviderDto> list() {
    return authConfigurationProvider.getConfigurations().stream()
        .map(Dtos::asSummaryDto)
        .collect(Collectors.toList());
  }
}
