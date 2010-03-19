/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.shell.commands;

import java.util.HashMap;
import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.datasource.excel.ExcelDatasource;
import org.obiba.magma.js.support.JavascriptMultiplexingStrategy;
import org.obiba.magma.js.support.JavascriptVariableTransformer;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.opal.core.service.ExportException;
import org.obiba.opal.core.service.ExportService;
import org.obiba.opal.shell.commands.options.CopyCommandOptions;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableSet;

/**
 * Provides ability to export Magma tables to an existing datasource or an Excel file.
 */
@CommandUsage(description = "Copy tables to an existing datasource or to the specified Excel file.", syntax = "Syntax: copy [--source NAME] (--destination NAME | --out FILE) [--multiplex SCRIPT] [--transform SCRIPT] [--catalogue]  [TABLE_NAME...]")
public class CopyCommand extends AbstractOpalRuntimeDependentCommand<CopyCommandOptions> {

  @Autowired
  private ExportService exportService;

  public void setExportService(ExportService exportService) {
    this.exportService = exportService;
  }

  public void execute() {
    if(options.getTables() != null && !options.isSource()) {
      if(validateOptions()) {
        Datasource destinationDatasource = null;
        try {
          if(options.isDestination()) {
            destinationDatasource = getDatasourceByName(options.getDestination());
          } else {
            if(options.getOut().exists() && options.getOut().canWrite() == false) {
              getShell().printf("Cannot write to file %s\n", options.getOut().getName());
              return;
            }
            destinationDatasource = new ExcelDatasource(options.getOut().getName(), options.getOut());
            MagmaEngine.get().addDatasource(destinationDatasource);
          }

          // build a datasource copier according to options
          DatasourceCopier.Builder builder = exportService.newCopier(destinationDatasource);

          if(options.isMultiplex()) {
            builder.withMultiplexingStrategy(new JavascriptMultiplexingStrategy(options.getMultiplex()));
          }

          if(options.isTransform()) {
            builder.withVariableTransformer(new JavascriptVariableTransformer(options.getTransform()));
          }

          if(options.getCatalogue()) {
            builder.dontCopyValues();
          }

          exportService.exportTablesToDatasource(getValueTables(), destinationDatasource, builder.build(), !options.getNonIncremental());

        } catch(ExportException e) {
          getShell().printf("%s\n", e.getMessage());
          e.printStackTrace(System.err);
        } catch(Exception e) {
          getShell().printf("%s\n", e.getMessage());
          e.printStackTrace(System.err);
        } finally {
          if(options.isOut() && destinationDatasource != null) {
            try {
              MagmaEngine.get().removeDatasource(destinationDatasource);
            } catch(RuntimeException e) {

            }
          }
        }
      }
    } else {
      getShell().printf("%s\n", "Neither source not table name(s) are specified.");
    }
  }

  private Set<ValueTable> getValueTables() {
    HashMap<String, ValueTable> names = new HashMap<String, ValueTable>();

    if(options.isSource()) {
      for(ValueTable table : getDatasourceByName(options.getSource()).getValueTables()) {
        names.put(table.getDatasource().getName() + "." + table.getName(), table);
      }
    }

    if(options.getTables() != null) {
      for(String name : options.getTables()) {
        if(!names.containsKey(name)) {
          names.put(name, MagmaEngineTableResolver.valueOf(name).resolveTable());
        }
      }
    }

    return ImmutableSet.copyOf(names.values());
  }

  private Datasource getDatasourceByName(String datasourceName) {
    return MagmaEngine.get().getDatasource(datasourceName);
  }

  private boolean validateOptions() {
    boolean validated = true;
    if(!options.isDestination() && !options.isOut()) {
      getShell().printf("Must provide either the 'destination' option or the 'out' option.\n");
      validated = false;
    }
    if(options.isDestination() && options.isOut()) {
      getShell().printf("The 'destination' option and the 'out' option are mutually exclusive.\n");
      validated = false;
    }
    if(options.isDestination()) {
      try {
        getDatasourceByName(options.getDestination());
      } catch(NoSuchDatasourceException e) {
        getShell().printf("Destination datasource '%s' does not exist.\n", options.getDestination());
        validated = false;
      }
    }
    if(options.isSource()) {
      try {
        getDatasourceByName(options.getSource());
      } catch(NoSuchDatasourceException e) {
        getShell().printf("Destination datasource '%s' does not exist.\n", options.getDestination());
        validated = false;
      }
    }
    if(options.getTables() != null) {
      for(String tableName : options.getTables()) {
        MagmaEngineTableResolver resolver = MagmaEngineTableResolver.valueOf(tableName);
        try {
          resolver.resolveTable();
        } catch(NoSuchDatasourceException e) {
          getShell().printf("'%s' refers to an unknown datasource: '%s'.\n", tableName, resolver.getDatasourceName());
          validated = false;
        } catch(NoSuchValueTableException e) {
          getShell().printf("Table '%s' does not exist in datasource : '%s'.\n", resolver.getTableName(), resolver.getDatasourceName());
          validated = false;
        }
      }
    }

    return validated;
  }

}
