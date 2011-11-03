/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.datashield;

import java.util.Map;
import java.util.NoSuchElementException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.obiba.opal.datashield.DataShieldLog;
import org.obiba.opal.datashield.DataShieldMethod;
import org.obiba.opal.datashield.cfg.DatashieldConfigurationSupplier;
import org.obiba.opal.r.ROperation;
import org.obiba.opal.r.ROperationTemplate;
import org.obiba.opal.r.ROperationWithResult;
import org.obiba.opal.r.RScriptROperation;
import org.obiba.opal.r.service.OpalRSession;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.web.r.OpalRSessionResource;
import org.obiba.opal.web.r.RSymbolResource;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class OpalDataShieldSessionResource extends OpalRSessionResource {

  private final DatashieldConfigurationSupplier configurationSupplier;

  private final ROperationTemplate clean;

  /**
   * @param opalRSessionManager
   * @param rSession
   */
  public OpalDataShieldSessionResource(ROperationTemplate clean, DatashieldConfigurationSupplier configurationSupplier, OpalRSessionManager opalRSessionManager, OpalRSession rSession) {
    super(opalRSessionManager, rSession);
    this.clean = clean;
    this.configurationSupplier = configurationSupplier;
  }

  @POST
  @Path("/aggregate/{method}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response aggregate(@PathParam("method") String methodName, @Context UriInfo uri, String body) {
    DataShieldLog.userLog("aggregating with '{}' on '{}' and arguments '{}'", methodName, body, uri.getQueryParameters());

    DataShieldMethod method;
    try {
      method = lookupMethod(methodName);
    } catch(NoSuchElementException e) {
      return Response.status(Status.NOT_FOUND).entity("No such method: " + methodName).build();
    }

    Map<String, REXP> arguments = evaluate(uri.getQueryParameters());

    RScriptROperation rop = new RScriptROperation(body);
    getOpalRSession().execute(rop);
    if(rop.hasRawResult()) {
      ROperationWithResult datashieldOperation = method.asOperation(rop.getRawResult(), arguments);
      clean.execute(datashieldOperation);
      return Response.ok().entity(datashieldOperation.getRawResult().asBytes()).build();
    }

    return Response.status(Status.INTERNAL_SERVER_ERROR).build();
  }

  private Map<String, REXP> evaluate(MultivaluedMap<String, String> parameters) {
    final Map<String, REXP> arguments = Maps.newHashMap();

    for(final String key : parameters.keySet()) {
      final String value = parameters.getFirst(key);
      getOpalRSession().execute(new ROperation() {

        @Override
        public void doWithConnection(RConnection connection) {
          try {
            arguments.put(key, connection.eval(value));
          } catch(RserveException e) {
            throw new RuntimeException(e);
          }
        }
      });
    }
    return arguments;
  }

  @Override
  protected RSymbolResource onGetRSymbolResource(String name) {
    return new DataShieldSymbolResource(this.configurationSupplier, getOpalRSession(), name);
  }

  @Override
  public Response query(String script) {
    return Response.noContent().build();
  }

  private Iterable<DataShieldMethod> listMethods() {
    return configurationSupplier.get().getAggregatingMethods();
  }

  private DataShieldMethod lookupMethod(final String methodName) {
    return Iterables.find(listMethods(), new Predicate<DataShieldMethod>() {

      @Override
      public boolean apply(DataShieldMethod input) {
        return input.getName().equalsIgnoreCase(methodName);
      }
    });
  }

}
