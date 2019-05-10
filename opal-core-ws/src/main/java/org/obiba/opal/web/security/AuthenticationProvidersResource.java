package org.obiba.opal.web.security;

import com.google.common.collect.Lists;
import org.obiba.oidc.OIDCConfiguration;
import org.obiba.oidc.OIDCConfigurationProvider;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.ws.security.NotAuthenticated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.List;

@Component
@Path("/auth/providers")
public class AuthenticationProvidersResource {

  @Autowired
  private OIDCConfigurationProvider authConfigurationProvider;

  @GET
  @NotAuthenticated
  public List<Opal.AuthProviderDto> getProviders() {

    List<Opal.AuthProviderDto> result = Lists.newArrayList();
    for (OIDCConfiguration config : authConfigurationProvider.getConfigurations()) {
      String label = config.getCustomParam("label");
      String providerUrl = config.getCustomParam("providerUrl");
      result.add(Dtos.asDto(config.getName(), label, providerUrl));
    }

    return result;
  }
}
