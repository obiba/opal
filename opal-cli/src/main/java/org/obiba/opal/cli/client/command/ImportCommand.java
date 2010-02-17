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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.opal.cli.client.command.options.ImportCommandOptions;
import org.obiba.opal.core.service.ImportService;
import org.springframework.beans.factory.annotation.Autowired;

@CommandUsage(description = "Imports a list of Onyx data files into a destination datasource. If a directory is provided, all .zip files inside of it are assumed to be imported.\n\nSyntax: import --datasource NAME --owner NAME [--encrypted] _FILE_...")
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
        System.err.println("No files found");
      }
    } else {
      System.err.println("No input (specify one or more directories of files to import)");
    }
  }

  //
  // Methods
  //

  private void importFiles(List<File> filesToImport) {
    String destination = options.getDatasource();

    System.out.println("Importing files");

    for(File file : filesToImport) {
      System.out.println("\t" + file.getPath());

      try {
        importService.importData(destination, options.getOwner(), file);
      } catch(NoSuchDatasourceException ex) {
        // Fatal exception - break out of here.
        System.err.println(ex.getMessage());
        break;
      } catch(IOException ex) {
        // Possibly a non-fatal exception - report an error and continue with the next file.
        System.err.println("I/O error: " + ex.getMessage());
        continue;
      }
    }
  }

  private List<File> resolveFiles() {
    List<File> files = new ArrayList<File>();

    for(File file : options.getFiles()) {
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
