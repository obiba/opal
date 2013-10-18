/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.shell;

import javax.ws.rs.Path;

import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.shell.CommandRegistry;
import org.obiba.opal.shell.service.CommandJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Backward compatibility.
 */
@Component
@Scope("request")
@Path("/datasource/{name}/commands")
public class DatasourceCommandsResource extends ProjectCommandsResource {

  @Autowired
  public DatasourceCommandsResource(OpalRuntime opalRuntime, CommandJobService commandJobService,
      @Qualifier("web") CommandRegistry commandRegistry) {
    super(opalRuntime,commandJobService,commandRegistry);
  }

}
