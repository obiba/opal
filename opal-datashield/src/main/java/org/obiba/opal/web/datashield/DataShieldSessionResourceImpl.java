/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.datashield;

import org.obiba.datashield.core.DSMethodType;
import org.obiba.datashield.r.expr.ParseException;
import org.obiba.opal.core.service.DataExportService;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.datashield.RestrictedRScriptROperation;
import org.obiba.opal.datashield.cfg.DataShieldProfile;
import org.obiba.opal.datashield.cfg.DataShieldProfileService;
import org.obiba.opal.r.service.RServerSession;
import org.obiba.opal.spi.r.ROperationWithResult;
import org.obiba.opal.spi.r.RSerialize;
import org.obiba.opal.web.r.AbstractRSessionResource;
import org.obiba.opal.web.r.RSymbolResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component("dataShieldSessionResource")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class DataShieldSessionResourceImpl extends AbstractRSessionResource implements DataShieldSessionResource {

  @Autowired
  private DataShieldProfileService datashieldProfileService;

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private IdentifiersTableService identifiersTableService;

  @Autowired
  private DataExportService dataExportService;

  @Override
  public Response aggregateBinary(@QueryParam("async") @DefaultValue("false") boolean async, String body) throws ParseException {
    return aggregate(async, body, RSerialize.RAW);
  }

  @Override
  public Response aggregateJSON(@QueryParam("async") @DefaultValue("false") boolean async, String body) throws ParseException {
    return aggregate(async, body, RSerialize.JSON);
  }

  private Response aggregate(boolean async, String body, RSerialize serialize) throws ParseException {
    RServerSession rSession = getRServerSession();
    DataShieldProfile profile = (DataShieldProfile) rSession.getProfile();
    ROperationWithResult operation = new RestrictedRScriptROperation(body,
        profile.getEnvironment(DSMethodType.AGGREGATE),
        datashieldProfileService.getRParserVersionOrDefault(profile),
        serialize);
    if (async) {
      String id = rSession.executeAsync(operation);
      return Response.ok().entity(id).type(MediaType.TEXT_PLAIN).build();
    } else if (serialize == RSerialize.RAW) {
      rSession.execute(operation);
      return Response.ok().entity(operation.getResult().asBytes()).type(MediaType.APPLICATION_OCTET_STREAM).build();
    } else {
      rSession.execute(operation);
      return Response.ok().entity(operation.getResult().asJSON()).type(MediaType.APPLICATION_JSON).build();
    }
  }

  @Override
  protected RSymbolResource onGetRSymbolResource(String name) {
    DataShieldSymbolResource resource = applicationContext
        .getBean("dataShieldSymbolResource", DataShieldSymbolResource.class);
    resource.setName(name);
    resource.setRServerSession(getRServerSession());
    resource.setIdentifiersTableService(identifiersTableService);
    resource.setDataExportService(dataExportService);
    resource.setResourceReferenceService(getResourceReferenceService());
    return resource;
  }

  @Override
  protected String getExecutionContext() {
    return DatashieldSessionsResourceImpl.DS_CONTEXT;
  }
}
