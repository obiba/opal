package org.obiba.opal.pac4j;

import io.buji.pac4j.ClientRealm;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.obiba.opal.pac4j.eid.EIdClient;
import org.pac4j.core.client.Clients;
import org.pac4j.core.exception.TechnicalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Initializes some aspects of pac4j, namely the callbackPath.
 */
@Component
public class Pac4jConfigurer {

    private static final Logger log = LoggerFactory.getLogger(Pac4jConfigurer.class);

    @Autowired
    private SecurityManager securityManager;

    @Value("${org.obiba.opal.public.url:http://localhost:8080}")
    private String opalPublicUrl;

    @Value("${org.obiba.opal.pac4j.clients.callbackPath:}")
    private String callbackPath;

    private boolean eidEnabled;

    @PostConstruct
    public void init() {

        if (!callbackPath.isEmpty()) {
            ClientRealm clientRealm = getClientRealm();
            Clients clients = null;

            if (clientRealm != null) {
                clients = clientRealm.getClients();
            }

            if (clients != null) {
                String callbackUrl = opalPublicUrl + callbackPath;
                clients.setCallbackUrl(callbackUrl);

                try {
                    this.eidEnabled = (clients.findClient(EIdClient.NAME) != null);
                } catch (TechnicalException ignored) {
                    //client not setup
                }
            }
        }
    }

    private ClientRealm getClientRealm() {
        if (securityManager instanceof RealmSecurityManager) {
            RealmSecurityManager rsm = (RealmSecurityManager)securityManager;
            for (Realm realm: rsm.getRealms()) {
                if (realm instanceof ClientRealm) {
                    return (ClientRealm)realm;
                }
            }
        }
        return null;
    }

    public boolean isEIdEnabled() {
        return eidEnabled;
    }

}
