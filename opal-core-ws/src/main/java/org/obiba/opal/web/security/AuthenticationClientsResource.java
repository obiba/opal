package org.obiba.opal.web.security;

import com.google.common.collect.Lists;
import org.obiba.opal.pac4j.Pac4jConfigurer;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.ws.security.NotAuthenticated;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.oidc.client.OidcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.List;

@Component
@Path("/authclients")
public class AuthenticationClientsResource {

  @Autowired
  private Pac4jConfigurer pac4jConfigurer;

  @GET
  @NotAuthenticated
  public List<Opal.AuthClientDto> getClients() {

    List<Opal.AuthClientDto> result = Lists.newArrayList();
    Clients clients = pac4jConfigurer.getConfig().getClients();
    for (Client client : clients.getClients()) {
      String label = null;
      if (client instanceof OidcClient) {
        label = ((OidcClient) client).getConfiguration().getCustomParam(Pac4jConfigurer.LABEL_KEY);
      }
      String redirectUrl = null;
      if (client instanceof IndirectClient) {
        redirectUrl = ((IndirectClient)client).getCallbackUrl();
      }
      result.add(Dtos.asDto(client.getName(), label, redirectUrl));
    }

    return result;
  }
}
