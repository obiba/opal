package org.obiba.opal.pac4j;

import io.buji.pac4j.ClientRealm;
import io.buji.pac4j.ShiroWebContext;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.security.Dtos;
import org.obiba.opal.web.ws.security.NotAuthenticated;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import java.util.ArrayList;
import java.util.List;

/**
 * Resource related to pac4j authentication clients
 */
@Component
@Path("/authclients")
public class AuthClientsResource {

    @Autowired
    private org.apache.shiro.mgt.SecurityManager securityManager;

    @Context
    private HttpServletRequest request;

    @Context
    private HttpServletResponse response;

    @GET
    @NotAuthenticated
    public List<Opal.AuthClientDto> getClients() {

        List<Opal.AuthClientDto> result = new ArrayList<>();
        ClientRealm clientRealm = Pac4jConfigurer.getClientRealm(securityManager);
        if (clientRealm != null) {
            Clients clients = clientRealm.getClients();
            if (clients != null) {
                ShiroWebContext context = new ShiroWebContext(request, response);
                for (Client client: clients.findAllClients()) {
                    if (client instanceof BaseClient) {
                        BaseClient bc = (BaseClient)client;
                        String url = bc.getRedirectionUrl(context);
                        if (url != null) {
                            result.add(Dtos.asDto(client.getName(), url));
                        }
                    }
                }
            }
        }

        return result;
    }

}
