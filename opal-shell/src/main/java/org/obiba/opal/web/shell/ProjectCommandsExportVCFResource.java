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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.commands.options.ExportVCFCommandOptions;
import org.obiba.opal.shell.web.ExportVCFCommandOptionsDtoImpl;
import org.obiba.opal.web.model.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/project/{name}/commands/_export_vcf")
@Tag(name = "Projects", description = "Operations on projects")
@Tag(name = "Tasks", description = "Operations on tasks")
public class ProjectCommandsExportVCFResource extends AbstractProjectCommandsResource {

  private static final Logger log = LoggerFactory.getLogger(ProjectCommandsExportVCFResource.class);

  @PathParam("name")
  protected String name;

  @Override
  public String getName() {
    return name;
  }

  @POST
  @Operation(
    summary = "Export VCF data",
    description = "Exports genomic data from the project to Variant Call Format (VCF) files for genomic analysis."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "VCF export command successfully launched"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions to access tables or write to destination"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response exportVCF(Commands.ExportVCFCommandOptionsDto optionsDto) {
    ExportVCFCommandOptions options = new ExportVCFCommandOptionsDtoImpl(optionsDto);
    Command<ExportVCFCommandOptions> command = commandRegistry.newCommand("export-vcf");
    command.setOptions(options);
    return launchCommand(command);
  }

}
