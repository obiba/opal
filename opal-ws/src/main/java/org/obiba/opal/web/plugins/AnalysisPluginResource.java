/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.plugins;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.spi.analysis.AnalysisService;
import org.obiba.plugins.spi.ServicePlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/analysis-plugin")
public class AnalysisPluginResource {

  private final OpalRuntime opalRuntime;

  @Autowired
  public AnalysisPluginResource(OpalRuntime opalRuntime) {
    this.opalRuntime = opalRuntime;
  }

  @GET
  @Path("/{plg}")
  public Response get(@PathParam("plg") String pluginName) {
    if (opalRuntime.hasServicePlugin(pluginName)) {
      ServicePlugin servicePlugin = opalRuntime.getServicePlugin(pluginName);

      if (servicePlugin instanceof AnalysisService) {
        return Response.ok(Dtos.asDto((AnalysisService) servicePlugin).build()).build();
      }

    }

    return Response.status(Status.NOT_FOUND).build();
  }
}
