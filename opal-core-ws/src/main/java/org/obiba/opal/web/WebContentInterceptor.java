/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web;

import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.HttpRequest;
import org.obiba.opal.web.ws.intercept.RequestCyclePostProcess;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Collections;

/**
 *
 */
@Component
public class WebContentInterceptor implements RequestCyclePostProcess {

    @Override
    public void postProcess(HttpServletRequest servletRequest, HttpRequest request, ResourceMethodInvoker resourceMethod, ServerResponse response) {
        try {
            MultivaluedMap<String, Object> map = response.getHeaders();
            //making sure IE doesn't cache: https://support.microsoft.com/en-us/kb/234067
            map.put("Expires", Collections.singletonList("-1"));
            map.put("Cache-Control", Collections.singletonList("no-cache"));
            map.put("Pragma", Collections.singletonList("no-cache"));
        } catch (Exception e) {
            // ignored
        }
    }
}
