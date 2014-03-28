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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.datashield.RestrictedRScriptROperation;
import org.obiba.opal.datashield.cfg.DatashieldConfigurationSupplier;
import org.obiba.opal.datashield.expr.DataShieldScriptValidator;
import org.obiba.opal.datashield.expr.FirstNodeInvokesFunctionValidator;
import org.obiba.opal.datashield.expr.NoBinaryOpsValidator;
import org.obiba.opal.datashield.expr.ParseException;
import org.obiba.opal.r.ROperationWithResult;
import org.obiba.opal.r.RScriptROperation;
import org.obiba.opal.web.r.OpalRSessionResourceImpl;
import org.obiba.opal.web.r.RSymbolResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("opalDataShieldSessionResource")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class OpalDataShieldSessionResourceImpl extends OpalRSessionResourceImpl
    implements OpalDataShieldSessionResource {

  @Autowired
  private DatashieldConfigurationSupplier configurationSupplier;

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private IdentifiersTableService identifiersTableService;

  @Override
  public Response aggregate(String body) {
    try {
      ROperationWithResult operation;
      switch(configurationSupplier.get().getLevel()) {
        case RESTRICTED:
          operation = new RestrictedRScriptROperation(body, configurationSupplier.get().getAggregateEnvironment(),
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
    DataShieldSymbolResource resource = applicationContext
        .getBean("dataShieldSymbolResource", DataShieldSymbolResource.class);
    resource.setName(name);
    resource.setOpalRSession(getOpalRSession());
    resource.setIdentifiersTableService(identifiersTableService);
    return resource;
  }

  @Override
  public Response execute(String script, boolean async, String body) {
    return Response.noContent().build();
  }

}
