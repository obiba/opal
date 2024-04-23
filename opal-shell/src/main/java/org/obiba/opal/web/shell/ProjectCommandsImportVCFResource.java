/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.shell;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.commands.options.ImportVCFCommandOptions;
import org.obiba.opal.shell.web.ImportVCFCommandOptionsDtoImpl;
import org.obiba.opal.web.model.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/project/{name}/commands/_import_vcf")
public class ProjectCommandsImportVCFResource extends AbstractProjectCommandsResource {

  private static final Logger log = LoggerFactory.getLogger(ProjectCommandsImportVCFResource.class);

  @PathParam("name")
  protected String name;

  @Override
  public String getName() {
    return name;
  }

  @POST
  public Response importVCF(Commands.ImportVCFCommandOptionsDto optionsDto) {
    ImportVCFCommandOptions options = new ImportVCFCommandOptionsDtoImpl(optionsDto);
    Command<ImportVCFCommandOptions> command = commandRegistry.newCommand("import-vcf");
    command.setOptions(options);
    return launchCommand(command);
  }

}
