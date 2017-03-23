/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.services;

import com.google.common.base.Strings;
import org.codehaus.jettison.json.JSONArray;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.spi.ServicePlugin;
import org.obiba.opal.spi.vcf.VCFStoreService;
import org.obiba.opal.web.model.Plugins;
import org.obiba.opal.web.plugins.Dtos;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope("request")
@Path("/plugins")
public class PluginsResource {

  @Autowired
  private OpalRuntime opalRuntime;

  @GET
  @NoAuthorization
  public List<Plugins.PluginDto> get(@QueryParam("type") String type) {
    return opalRuntime.getPlugins().stream()
        .map(Dtos::asDto)
        .filter(p -> Strings.isNullOrEmpty(type) || (type.equals(p.getType()) || testType(type)))
        .collect(Collectors.toList());
  }

  private boolean testType(String type) {
    switch (type) {
      case VCFStoreService.SERVICE_TYPE:
        return true;
    }

    return false;
  }

}
