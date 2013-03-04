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

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.obiba.opal.datashield.RestrictedRScriptROperation;
import org.obiba.opal.datashield.cfg.DatashieldConfigurationSupplier;
import org.obiba.opal.datashield.expr.DataShieldScriptValidator;
import org.obiba.opal.datashield.expr.FirstNodeInvokesFunctionValidator;
import org.obiba.opal.datashield.expr.NoBinaryOpsValidator;
import org.obiba.opal.datashield.expr.ParseException;
import org.obiba.opal.r.ROperationWithResult;
import org.obiba.opal.r.RScriptROperation;
import org.obiba.opal.r.service.OpalRSession;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.web.r.OpalRSessionResource;
import org.obiba.opal.web.r.RSymbolResource;

public class OpalDataShieldSessionResource extends OpalRSessionResource {

  private final DatashieldConfigurationSupplier configurationSupplier;

  /**
   * @param opalRSessionManager
   * @param rSession
   */
  public OpalDataShieldSessionResource(DatashieldConfigurationSupplier configurationSupplier,
      OpalRSessionManager opalRSessionManager, OpalRSession rSession) {
    super(opalRSessionManager, rSession);
    this.configurationSupplier = configurationSupplier;
  }

  @POST
  @Path("/aggregate")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response aggregate(String body) {
    try {
      ROperationWithResult operation;
      switch(configurationSupplier.get().getLevel()) {
        case RESTRICTED:
          operation = new RestrictedRScriptROperation(body, this.configurationSupplier.get().getAggregateEnvironment(),
              DataShieldScriptValidator.of(new FirstNodeInvokesFunctionValidator(), new NoBinaryOpsValidator()));
          break;
        case UNRESTRICTED:
          operation = new RScriptROperation(body);
          break;
        default:
          throw new IllegalStateException(
              "Unknown script interpretation level: " + configurationSupplier.get().getLevel());
      }
      getOpalRSession().execute(operation);
      return Response.ok().entity(operation.getRawResult().asBytes()).build();
    } catch(ParseException e) {
      return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
    }

  }

  @Override
  protected RSymbolResource onGetRSymbolResource(String name) {
    return new DataShieldSymbolResource(this.configurationSupplier, getOpalRSession(), name);
  }

  @Override
  public Response execute(String script, String body) {
    return Response.noContent().build();
  }

}
