package org.obiba.opal.pac4j;

import io.buji.pac4j.ClientFilter;
import io.buji.pac4j.NoAuthenticationException;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.obiba.shiro.web.filter.AuthenticationExecutor;
import org.pac4j.core.exception.RequiresHttpAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.annotation.PostConstruct;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * Filter for handling the pac4j callback.
 * After successful pac4j/shiro/opal login, its redirected to another page
 */
@Component("pac4jClientFilter")
public class Pac4jClientFilter extends ClientFilter {

    private static final Logger log = LoggerFactory.getLogger(Pac4jClientFilter.class);

    @Autowired
    private org.apache.shiro.mgt.SecurityManager securityManager;

    @Autowired
    private AuthenticationExecutor authenticationExecutor;

    @Value("${org.obiba.opal.public.url:http://localhost:8080}")
    private String opalPublicUrl;

    @Value("${org.obiba.shiro.authenticationFilter.cookie.sessionId}")
    private String sessionIdCookieName;

    @PostConstruct
    public void init() {
        if (Pac4jConfigurer.isEnabled()) {
            setClients(Pac4jConfigurer.getClients(securityManager));
            processPathConfig(Pac4jConfigurer.getCallbackPath(), null);
        }
    }

    @Override
    public String getSuccessUrl() {
        //@TODO: find the correct/best url to redirect after successful login
        //String path = AuthenticationResource.getSucessfulLoginPath();
        String path = "/";
        return opalPublicUrl + path;
    }

    /**
     * This override is required so we call authenticationExecutor.login and add session cookie to response.
     * Without this, subject is authenticated in Shiro, but not in Opal
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        final AuthenticationToken token;
        try {
            token = createToken(request, response);
        } catch (final RequiresHttpAction e) {
            log.debug("requires HTTP action : {}", e);
            return false;
        }

        try {
            final Subject subject = authenticationExecutor.login(token);
            HttpServletResponse res = (HttpServletResponse)response;
            String sessionId = SecurityUtils.getSubject().getSession().getId().toString();
            res.addCookie(new Cookie(sessionIdCookieName, sessionId));
            return onLoginSuccess(token, subject, request, response);

        } catch (final NoAuthenticationException e) {
            // no authentication happens but go to the success url however :
            // the protecting filter will have the appropriate behaviour
            return onLoginSuccess(token, null, request, response);
        } catch (final AuthenticationException e) {
            return onLoginFailure(token, e, request, response);
        }
    }

    public static class Wrapper extends DelegatingFilterProxy {
        public Wrapper() {
            super("pac4jClientFilter");
        }
    }

}
