/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.r;

import com.google.common.base.Strings;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.core.Response;

/**
 * Handles web services on a particular R session of the invoking Opal user.
 */
@Component("opalRSessionResource")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class OpalRSessionResourceImpl extends AbstractRSessionResource implements OpalRSessionResource {

  @Override
  public Response execute(String script, boolean async, String body) {
    String rScript = script;
    if(Strings.isNullOrEmpty(rScript)) {
      rScript = body;
    }
    return RSessionResourceHelper.executeScript(getOpalRSession(), rScript, async);
  }

}
