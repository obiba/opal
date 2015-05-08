package org.obiba.opal.web;

import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.HttpRequest;
import org.obiba.opal.web.ws.intercept.RequestCyclePostProcess;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Collections;

/**
 *
 */
@Component
public class WebContentInterceptor implements RequestCyclePostProcess {

    @Override
    public void postProcess(HttpRequest request, ResourceMethodInvoker resourceMethod, ServerResponse response) {
        MultivaluedMap<String, Object> map = response.getHeaders();
        //making sure IE doesn't cache: https://support.microsoft.com/en-us/kb/234067
        map.put("Expires", Collections.<Object>singletonList("-1"));
        map.put("Cache-Control", Collections.<Object>singletonList("no-cache"));
        map.put("Pragma", Collections.<Object>singletonList("no-cache"));
    }
}
