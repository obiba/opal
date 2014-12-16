package org.obiba.opal.pac4j;

import io.buji.pac4j.filter.ClientUserFilter;
import org.apache.shiro.web.servlet.OncePerRequestFilter;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.annotation.PostConstruct;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Filter that handles the pac4j redirect requests.<br>
 * The goal of this filter is always to redirect, either to an external identity provider,
 * or to / (if client not recognized).
 * Each client will have one redirect url, in the form <public_opal_url>/<base_path>/<client_name>.<br>
 * The redirect url will therefore indicate which client filter will the request be delegated to.
 */
@Component("pac4jMultiClientFilter")
public class Pac4jMultiClientUserFilter extends OncePerRequestFilter {

    /**
     * Base path for all pac4j redirect urls
     */
    public static final String DEFAULT_BASE_PATH = "/auth";

    @Autowired
    private org.apache.shiro.mgt.SecurityManager securityManager;

    @Value("${org.obiba.opal.pac4j.clients.basePath:/auth}")
    private String basePath;

    private Map<String,Filter> filterMap = new HashMap<>();

    @Override
    protected void doFilterInternal(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {

        boolean found = false;
        HttpServletRequest req = (HttpServletRequest)request;
        String uri = req.getRequestURI();
        int idx = uri.lastIndexOf('/');
        if (idx > 0 && idx < uri.length() - 1) {
            String client = uri.substring(idx + 1);
            Filter filter = filterMap.get(client);
            if (filter != null) {
                found = true;
                filter.doFilter(request, response, chain); //delegates to the proper client filter
            }
        }

        if (!found) {
            String url = req.getRequestURL().toString();
            int idx2 = url.indexOf(uri);
            String target = url.substring(0, idx2);
            HttpServletResponse resp = (HttpServletResponse)response;
            resp.sendRedirect(target); //no client found: just redirects to /
        }
    }

    /**
     * @param client
     * @return the path for the given client
     */
    private String getPath(String client) {
        return basePath + "/" + client;
    }

    @Override
    public void destroy() {
        for (Filter filter: filterMap.values()) {
            filter.destroy();
        }
    }

    /**
     * Initializes all the client user filters defined on shiro.ini, setting all the ba
     */
    @PostConstruct
    public void initClientUserFilters() {
        for (Client client: Pac4jConfigurer.getClientList(securityManager)) {
            if (client instanceof BaseClient) {
                String name = client.getName();
                ClientUserFilter filter = new ClientUserFilter();
                filter.setClient((BaseClient)client);
                filter.processPathConfig(getPath(name), null);
                filterMap.put(name, filter);
            }
        }
    }

    public static class Wrapper extends DelegatingFilterProxy {
        public Wrapper() {
            super("pac4jMultiClientFilter");
        }
    }

}
