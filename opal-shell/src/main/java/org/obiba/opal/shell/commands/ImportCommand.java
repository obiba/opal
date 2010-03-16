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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.opal.core.service.ImportService;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.shell.commands.options.ImportCommandOptions;
import org.springframework.beans.factory.annotation.Autowired;

@CommandUsage(description = "Imports one or more Onyx data files into a datasource.", syntax = "Syntax: import --destination NAME --owner NAME _FILE_...")
public class ImportCommand extends AbstractOpalRuntimeDependentCommand<ImportCommandOptions> {

  //
  // AbstractContextLoadingCommand Methods
  //

  @Autowired
  private ImportService importService;

  public void execute() {
    if(options.isFiles()) {
      List<File> filesToImport = resolveFiles();
      if(!filesToImport.isEmpty()) {
        importFiles(filesToImport);
      } else {
        getShell().printf("No file found. Import canceled.\n");
      }
    } else {
      // TODO: When no file is specified, import all files in the specified unit's directory.
      getShell().printf("No file found. Import canceled.\n");
    }
  }

  //
  // Methods
  //

  private void importFiles(List<File> filesToImport) {
    String destination = options.getDestination();

    getShell().printf("Importing %d file%s :\n", filesToImport.size(), (filesToImport.size() > 1 ? "s" : ""));
    for(File file : filesToImport) {
      getShell().printf("  %s\n", file.getPath());

      try {
        importService.importData(options.getUnit(), destination, file);
      } catch(NoSuchFunctionalUnitException ex) {
        // Fatal exception - break out of here.
        getShell().printf("Functional unit '%s' does not exist. Cannot import.\n", ex.getUnitName());
        break;
      } catch(NoSuchDatasourceException ex) {
        // Fatal exception - break out of here.
        getShell().printf("Destination datasource '%s' does not exist. Cannot import.\n", ex.getDatasourceName());
        break;
      } catch(IOException ex) {
        // Possibly a non-fatal exception - report an error and continue with the next file.
        getShell().printf("Unrecoverable import exception: %s\n", ex.getMessage());
        ex.printStackTrace(System.err);
        continue;
      }
    }
  }

  private List<File> resolveFiles() {
    List<File> files = new ArrayList<File>();

    for(File file : options.getFiles()) {
      if(file.exists() == false) {
        getShell().printf("'%s' does not exist.\n", file.getPath());
        continue;
      }
      if(file.isDirectory()) {
        File[] filesInDir = file.listFiles(new FileFilter() {
          public boolean accept(File dirFile) {
            return dirFile.isFile() && dirFile.getName().toLowerCase().endsWith(".zip");
          }
        });
        files.addAll(Arrays.asList(filesInDir));
      } else if(file.isFile()) {
        files.add(file);
      }
    }

    return files;
  }
}
