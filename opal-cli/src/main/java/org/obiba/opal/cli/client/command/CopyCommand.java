/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.opal.cli.client.command;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import org.obiba.opal.cli.client.command.options.CopyCommandOptions;
import org.obiba.opal.core.service.ExportException;
import org.obiba.opal.core.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableSet;

/**
 * Provides ability to export Magma tables to an existing datasource or an Excel file.
 */
@CommandUsage(description = "Copy tables to an existing destination datasource or to a specified Excel file. The tables can be explicitly named and/or be the ones from a specified source datasource. The variables can be optionally processed: dispatched in another table and/or renamed.", syntax = "Syntax: copy [--source NAME] (--destination NAME | --out FILE) [--multiplex SCRIPT] [--transform SCRIPT] [--nonIncremental] [--catalogue] [TABLE_NAME...]")
public class CopyCommand extends AbstractOpalRuntimeDependentCommand<CopyCommandOptions> {

  @Autowired
  private ExportService exportService;

  public void execute() {
    if(options.getTables() != null && !options.isSource()) {
      if(validateOptions()) {
        Datasource destinationDatasource = null;
        try {
          if(options.isDestination()) {
            destinationDatasource = getDatasourceByName(options.getDestination());
          } else {
            File outputFile = getOuputFile();
            destinationDatasource = new ExcelDatasource(outputFile.getName(), outputFile);
            MagmaEngine.get().addDatasource(destinationDatasource);
          }

          // build a datasource copier according to options
          DatasourceCopier.Builder builder;
          if(options.getCatalogue()) {
            builder = DatasourceCopier.Builder.newCopier().dontCopyValues();
          } else {
            // get a builder with logging facilities
            builder = exportService.newCopier(destinationDatasource);
          }

          if(options.isMultiplex()) {
            builder.withMultiplexingStrategy(new JavascriptMultiplexingStrategy(options.getMultiplex()));
          }

          if(options.isTransform()) {
            builder.withVariableTransformer(new JavascriptVariableTransformer(options.getTransform()));
          }

          exportService.exportTablesToDatasource(getValueTables(), destinationDatasource, builder.build(), !options.getNonIncremental());

        } catch(ExportException e) {
          System.console().printf("%s\n", e.getMessage());
          System.err.println(e);
        } catch(Exception e) {
          System.console().printf("%s\n", e.getMessage());
          System.err.println(e);
        } finally {
          if(options.isOut() && destinationDatasource != null) {
            MagmaEngine.get().removeDatasource(destinationDatasource);
          }
        }
      }
    } else {
      System.console().printf("%s\n", "Neither source, nor table name(s) are specified.");
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

  /**
   * Get the output file to which the metadata will be exported to.
   * 
   * @return The output file.
   */
  private File getOuputFile() {

    // Get the file specified on the command line.
    if(options.isOut()) {
      File outputFile = options.getOut();

      // Create the parent directory, if it doesn't already exist.
      File directory = outputFile.getParentFile();
      if(directory != null && !directory.exists()) {
        directory.mkdirs();
      }
      if(options.getOut().getName().endsWith("xls")) {
        System.console().printf("WARNING: Writing to an Excel 97 spreadsheet. These are limited to 256 columns and 65536 rows which may not be sufficient for writing large tables.\nUse an 'xlsx' extension to use Excel 2007 format which supports 16K columns.\n");
      }
      return options.getOut();
    } else {
      // Generate a file name automatically when not specified by user.
      SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
      return new File("variables-" + dateFormatter.format(new Date()) + ".xlsx");
    }

  }

}
