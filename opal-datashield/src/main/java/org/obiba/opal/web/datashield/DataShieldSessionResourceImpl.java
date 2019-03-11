/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.datashield;

import org.obiba.datashield.core.DSMethodType;
import org.obiba.datashield.r.expr.DSRScriptValidator;
import org.obiba.datashield.r.expr.FirstNodeInvokesFunctionValidator;
import org.obiba.datashield.r.expr.NoBinaryOpsValidator;
import org.obiba.datashield.r.expr.ParseException;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.datashield.RestrictedRScriptROperation;
import org.obiba.opal.datashield.cfg.DatashieldConfigurationSupplier;
import org.obiba.opal.spi.r.ROperationWithResult;
import org.obiba.opal.spi.r.RScriptROperation;
import org.obiba.opal.web.r.AbstractRSessionResource;
import org.obiba.opal.web.r.RSymbolResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Component("dataShieldSessionResource")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class DataShieldSessionResourceImpl extends AbstractRSessionResource implements DataShieldSessionResource {

  @Autowired
  private DatashieldConfigurationSupplier configurationSupplier;

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private IdentifiersTableService identifiersTableService;

  @Autowired
  private TransactionTemplate txTemplate;

  @Override
  public Response aggregate(@QueryParam("async") @DefaultValue("false") boolean async, String body) {
    try {
      ROperationWithResult operation;
      switch(configurationSupplier.get().getLevel()) {
        case RESTRICTED:
          operation = new RestrictedRScriptROperation(body, configurationSupplier.get().getEnvironment(DSMethodType.AGGREGATE),
              DSRScriptValidator.of(new FirstNodeInvokesFunctionValidator(), new NoBinaryOpsValidator()));
          break;
        case UNRESTRICTED:
          operation = new RScriptROperation(body);
          break;
        default:
          throw new IllegalStateException(
              "Unknown script interpretation level: " + configurationSupplier.get().getLevel());
      }
      if (async) {
        String id = getOpalRSession().executeAsync(operation);
        return Response.ok().entity(id).type(MediaType.TEXT_PLAIN).build();
      } else {
        getOpalRSession().execute(operation);
        return Response.ok().entity(operation.getRawResult().asBytes()).build();
      }
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
    resource.setTransactionTemplate(txTemplate);
    return resource;
  }

}
