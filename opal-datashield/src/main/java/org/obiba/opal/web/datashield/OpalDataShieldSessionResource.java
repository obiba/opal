/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.datashield;

import java.util.NoSuchElementException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.datashield.DataShieldLog;
import org.obiba.opal.datashield.DataShieldMethod;
import org.obiba.opal.datashield.cfg.DatashieldConfiguration;
import org.obiba.opal.r.ROperationTemplate;
import org.obiba.opal.r.ROperationWithResult;
import org.obiba.opal.r.RScriptROperation;
import org.obiba.opal.r.service.OpalRSession;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.web.r.OpalRSessionResource;
import org.obiba.opal.web.r.RSymbolResource;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class OpalDataShieldSessionResource extends OpalRSessionResource {

  private final OpalConfigurationService configService;

  private final ROperationTemplate clean;

  /**
   * @param opalRSessionManager
   * @param rSession
   */
  public OpalDataShieldSessionResource(ROperationTemplate clean, OpalConfigurationService configService, OpalRSessionManager opalRSessionManager, OpalRSession rSession) {
    super(opalRSessionManager, rSession);
    this.clean = clean;
    this.configService = configService;
  }

  @POST
  @Path("/aggregate/{method}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response aggregate(@PathParam("method") String methodName, String script) {
    DataShieldLog.userLog("aggregating with '{}' on '{}'", methodName, script);

    DataShieldMethod method;
    try {
      method = lookupMethod(methodName);
    } catch(NoSuchElementException e) {
      return Response.status(Status.NOT_FOUND).entity("No such method: " + methodName).build();
    }

    RScriptROperation rop = new RScriptROperation(script);
    getOpalRSession().execute(rop);
    if(rop.hasRawResult()) {
      ROperationWithResult datashieldOperation = method.asOperation(rop.getRawResult());
      clean.execute(datashieldOperation);
      return Response.ok().entity(datashieldOperation.getRawResult().asBytes()).build();
    }

    return Response.status(Status.INTERNAL_SERVER_ERROR).build();
  }

  @Override
  protected RSymbolResource onGetRSymbolResource(String name) {
    return new DataShieldSymbolResource(getOpalRSession(), name);
  }

  @Override
  public Response query(String script) {
    return Response.noContent().build();
  }

  private Iterable<DataShieldMethod> listMethods() {
    OpalConfiguration cfg = configService.getOpalConfiguration();
    DatashieldConfiguration datashieldConfig = cfg.getExtension(DatashieldConfiguration.class);
    return datashieldConfig.getAggregatingMethods();
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
