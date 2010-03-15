/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.cli.client.command;

import java.util.ArrayList;
import java.util.List;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.js.support.JavascriptMultiplexingStrategy;
import org.obiba.magma.js.support.JavascriptVariableTransformer;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.opal.cli.client.command.options.CopyCommandOptions;
import org.obiba.opal.core.service.ExportException;
import org.obiba.opal.core.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Provides ability to export Magma tables to an existing datasource or an Excel file.
 */
@CommandUsage(description = "Copy tables to an existing datasource or to the specified Excel file.", syntax = "Syntax: copy (--destination NAME | --out FILE) [--multiplex] [--transform] [--catalogue] [--source NAME | TABLE_NAME...]")
public class CopyCommand extends AbstractOpalRuntimeDependentCommand<CopyCommandOptions> {

  @Autowired
  private ExportService exportService;

  public void execute() {
    if(options.getTables() != null && !options.isSource()) {
      if(validateOptions()) {
        try {
          if(!options.isMultiplex() && !options.isTransform()) {
            if(options.isDestination()) {
              exportService.exportTablesToDatasource(getTableNames(), options.getDestination(), !options.getNonIncremental());
            } else if(options.isOut()) {
              exportService.exportTablesToExcelFile(getTableNames(), options.getOut(), !options.getNonIncremental());
            }
          } else {
            // build a datasource copier according to options
            DatasourceCopier.Builder builder = DatasourceCopier.Builder.newCopier().withLoggingListener();

            if(options.isMultiplex()) {
              builder.withMultiplexingStrategy(new JavascriptMultiplexingStrategy(options.getMultiplex()));
            }

            if(options.isTransform()) {
              builder.withVariableTransformer(new JavascriptVariableTransformer(options.getTransform()));
            }

            if(options.getCatalogue()) {
              builder.dontCopyValues();
            }

            if(options.isDestination()) {
              exportService.exportTablesToDatasource(getTableNames(), options.getDestination(), builder.build(), !options.getNonIncremental());
            } else if(options.isOut()) {
              exportService.exportTablesToExcelFile(getTableNames(), options.getOut(), builder.build(), !options.getNonIncremental());
            }
          }
        } catch(ExportException e) {
          System.console().printf("%s\n", e.getMessage());
          System.err.println(e);
        } catch(UnsupportedOperationException e) {
          System.console().printf("%s\n", e.getMessage());
        }
      }
    } else {
      System.console().printf("%s\n", "Neither source not table name(s) are specified.");
    }
  }

  private List<String> getTableNames() {
    ArrayList<String> names = new ArrayList<String>();

    if(options.isSource()) {
      for(ValueTable table : getDatasourceByName(options.getSource()).getValueTables()) {
        names.add(options.getSource() + "." + table.getName());
      }
    }

    if(options.getTables() != null) {
      for(String name : options.getTables()) {
        if(!names.contains(name)) {
          names.add(name);
        }
      }
    }

    return names;
  }

  private Datasource getDatasourceByName(String datasourceName) {
    return MagmaEngine.get().getDatasource(datasourceName);
  }

  private boolean validateOptions() {
    boolean validated = true;
    if(!options.isDestination() && !options.isOut()) {
      System.console().printf("Must provide either the 'destination' option or the 'out' option.\n");
      validated = false;
    }
    if(options.isDestination() && options.isOut()) {
      System.console().printf("The 'destination' option and the 'out' option are mutually exclusive.\n");
      validated = false;
    }
    if(options.isDestination()) {
      try {
        getDatasourceByName(options.getDestination());
      } catch(NoSuchDatasourceException e) {
        System.console().printf("Destination datasource '%s' does not exist.\n", options.getDestination());
        validated = false;
      }
    }
    if(options.isSource()) {
      try {
        getDatasourceByName(options.getSource());
      } catch(NoSuchDatasourceException e) {
        System.console().printf("Destination datasource '%s' does not exist.\n", options.getDestination());
        validated = false;
      }
    }
    if(options.getTables() != null) {
      for(String tableName : options.getTables()) {
        MagmaEngineTableResolver resolver = MagmaEngineTableResolver.valueOf(tableName);
        try {
          resolver.resolveTable();
        } catch(NoSuchDatasourceException e) {
          System.console().printf("'%s' refers to an unknown datasource: '%s'.\n", tableName, resolver.getDatasourceName());
          validated = false;
        } catch(NoSuchValueTableException e) {
          System.console().printf("Table '%s' does not exist in datasource : '%s'.\n", resolver.getTableName(), resolver.getDatasourceName());
          validated = false;
        }
      }
    }

    return validated;
  }

}