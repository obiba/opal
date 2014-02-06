/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.r;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.r.StringAssignROperation;
import org.obiba.opal.r.service.OpalRSession;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.web.model.OpalR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;

/**
 * Handles web services on a particular R session of the invoking Opal user.
 */
@Component("opalRSessionResource")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class OpalRSessionResourceImpl extends AbstractOpalRSessionResource implements OpalRSessionResource {

  @Autowired
  private OpalRSessionManager opalRSessionManager;

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private IdentifiersTableService identifiersTableService;

  private OpalRSession rSession;

  @Override
  public void setOpalRSession(OpalRSession rSession) {
    this.rSession = rSession;
  }

  @Override
  public OpalR.RSessionDto getRSession() {
    return Dtos.asDto(rSession);
  }

  @Override
  public Response removeRSession() {
    opalRSessionManager.removeSubjectRSession(rSession.getId());
    return Response.ok().build();
  }

  @Override
  public Response setCurrentRSession() {
    opalRSessionManager.setSubjectCurrentRSession(rSession.getId());
    return Response.ok().build();
  }

  @Override
  public Response execute(String script, String body) {
    String rScript = script;
    if(Strings.isNullOrEmpty(rScript)) {
      rScript = body;
    }
    return executeScript(rSession, rScript);
  }

  @Override
  public Response ls() {
    return executeScript(rSession, "base::ls()");
  }

  @Override
  public Response assign(MultivaluedMap<String, String> symbols) {
    rSession.execute(new StringAssignROperation(symbols));
    return ls();
  }

  @Override
  public RSymbolResource getRSymbolResource(String name) {
    return onGetRSymbolResource(name);
  }

  protected RSymbolResource onGetRSymbolResource(String name) {
    SecuredRSymbolResource resource = applicationContext
        .getBean("securedRSymbolResource", SecuredRSymbolResource.class);
    resource.setName(name);
    resource.setOpalRSession(rSession);
    resource.setIdentifiersTableService(identifiersTableService);
    return resource;
  }

  protected OpalRSession getOpalRSession() {
    return rSession;
  }

}
