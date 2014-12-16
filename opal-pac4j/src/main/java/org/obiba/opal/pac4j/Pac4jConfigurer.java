package org.obiba.opal.pac4j;

import io.buji.pac4j.ClientRealm;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Initializes some aspects of pac4j, namely the callback path and url.
 * The callback url is defined by appending the opal public url to the callback path.
 */
@Component
public class Pac4jConfigurer {

    //private static final String CALLBACK_URL = "org.obiba.opal.pac4j.clients.callbackUrl";
    private static String callbackUrl;
    private static String callbackPath;
    private static boolean enabled;

    /**
     * Initializes based on opal-config properties
     * @param properties
     * @return true if pac4j was enabled (according to properties)
     */
    public static boolean init(Properties properties) {
        String path = properties.getProperty("org.obiba.opal.pac4j.clients.callbackPath");

        if (path != null) {
            String opalPublicUrl = properties.getProperty("org.obiba.opal.public.url", "http://localhost:8080");
            String url = opalPublicUrl + path;
            callbackUrl = url;
            callbackPath = path;
            enabled = true;
        }
        return enabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * @return callback url (same for all identity providers)
     * @throws java.lang.IllegalStateException if pac4j is not enabled
     */
    public static String getCallbackUrl() {
        if (!enabled) {
            throw new IllegalStateException("Pac4j is not enabled/setup");
        }
        return callbackUrl;
    }

    /**
     * @return callback url (same for all identity providers)
     * @throws java.lang.IllegalStateException if pac4j is not enabled
     */
    public static String getCallbackPath() {
        if (!enabled) {
            throw new IllegalStateException("Pac4j is not enabled/setup");
        }
        return callbackPath;
    }

    public static ClientRealm getClientRealm(SecurityManager securityManager) {
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

    public static Clients getClients(SecurityManager securityManager) {
        ClientRealm clientRealm = getClientRealm(securityManager);
        if (clientRealm != null) {
            return clientRealm.getClients();
        }
        return null;
    }

    public static List<Client> getClientList(SecurityManager securityManager) {
        Clients clients = getClients(securityManager);

        if (clients != null) {
            return clients.findAllClients();
        }

        return Collections.emptyList();
    }

}
