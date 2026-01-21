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
import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.commands.options.ImportCommandOptions;
import org.obiba.opal.shell.web.ImportCommandOptionsDtoImpl;
import org.obiba.opal.web.model.Commands;
import org.obiba.opal.web.support.ConflictingRequestException;
import org.obiba.opal.web.support.InvalidRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@Scope("request")
@Path("/project/{name}/commands/_import")
@Tag(name = "Projects", description = "Operations on projects")
@Tag(name = "Tasks", description = "Operations on tasks")
public class ProjectCommandsImportResource extends AbstractProjectCommandsResource {

  private static final Logger log = LoggerFactory.getLogger(ProjectCommandsImportResource.class);

  @PathParam("name")
  protected String name;

  @Override
  public String getName() {
    return name;
  }

  @POST
  @Operation(
    summary = "Import data to project",
    description = "Imports data from various sources (files, databases, other projects) into the current project."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Import command successfully launched"),
    @ApiResponse(responseCode = "409", description = "Project is momentarily not reloadable"),
    @ApiResponse(responseCode = "400", description = "Invalid destination or import configuration"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions to access source or write to tables"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response importData(Commands.ImportCommandOptionsDto options) {
    if (checkCommandIsBlocked(name, false)) throw new ConflictingRequestException("ProjectMomentarilyNotReloadable", name);

    if(!name.equals(options.getDestination())) {
      throw new InvalidRequestException("DataCanOnlyBeImportedInCurrentDatasource", name);
    }

    // check import does not override an existing but not visible table
    if (options.getTablesCount()>0) {
      ensureTableWriteAccess(options.getDestination(),
          options.getTablesList().stream().map(tablePath -> MagmaEngineTableResolver.valueOf(tablePath).getTableName()).collect(Collectors.toList()));
    }
    if (options.hasSource()) {
      Datasource ds = getDatasourceOrTransientDatasource(options.getSource());
      ensureTableWriteAccess(options.getDestination(),
          ds.getValueTables().stream().map(ValueTable::getName).collect(Collectors.toList()));
    }


    ImportCommandOptions importOptions = new ImportCommandOptionsDtoImpl(options);
    Command<ImportCommandOptions> importCommand = commandRegistry.newCommand("import");
    importCommand.setOptions(importOptions);

    return launchCommand(importCommand);
  }

}
