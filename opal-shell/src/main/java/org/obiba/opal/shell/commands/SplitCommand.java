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
import java.io.IOException;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.ValueTableWriter.ValueSetWriter;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.shell.commands.options.SplitCommandOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@CommandUsage(description = "Splits one or more ZIP files into multiple pieces.", syntax = "Syntax: split --unit unit --out DIR _FILE_...")
public class SplitCommand extends AbstractOpalRuntimeDependentCommand<SplitCommandOptions> {

  private static final Logger log = LoggerFactory.getLogger(SplitCommand.class);

  public void execute() {
    // Ensure that options have been set.
    if(options.getFiles() == null) {
      getShell().printf("No input file(s) specified.\n");
      return;
    }
    try {
      innerExecute();
    } catch(FileSystemException e) {
      // We can't handle it
      throw new RuntimeException(e);
    }
  }

  private void innerExecute() throws FileSystemException {

    FunctionalUnit unit = getOpalRuntime().getFunctionalUnit(options.getUnit());
    if(unit == null) {
      getShell().printf("Functional unit '%s' does not exist.\n", options.getUnit());
      return;
    }

    FileObject unitDir = getOpalRuntime().getUnitDirectory(options.getUnit());

    FileObject outputDir = resolveFile(options.getOutput(), unitDir);
    if(outputDir.exists() == false) {
      try {
        outputDir.createFolder();
      } catch(FileSystemException e) {
        getShell().printf("Could not create output directory: %s\n", outputDir.getName().getPath());
        return;
      }
    } else if(outputDir.getType() != FileType.FOLDER) {
      getShell().printf("Specified output '%s' is not a directory.\n", outputDir.getName().getPath());
      return;
    }

    for(String inputName : options.getFiles()) {
      FileObject inputFile = resolveFile(inputName, unitDir);
      if(inputFile.exists() == false) {
        getShell().printf("Skipping non-existant input file %s\n", inputFile.getName());
      } else {
        getShell().printf("Splitting input file %s in chunks of %d entities\n", inputFile.getName().getPath(), options.getChunkSize());
        File localInputFile = getOpalRuntime().getFileSystem().getLocalFile(inputFile);
        processFile(unit, localInputFile, outputDir);
      }
    }
  }

  private FileObject resolveFile(String filename, FileObject unitDir) {
    FileObject outputDir = null;
    FileObject root = getOpalRuntime().getFileSystem().getRoot();
    try {
      if(filename.startsWith("/")) {
        outputDir = root.resolveFile(filename);
      } else {
        outputDir = unitDir.resolveFile(filename);
      }
    } catch(FileSystemException e) {
      outputDir = null;
    }
    return outputDir;
  }

  private void processFile(FunctionalUnit unit, File inputFile, FileObject outputDir) {
    String inputFilename = inputFile.getName();
    FsDatasource inputDatasource = new FsDatasource(inputFilename, inputFile, unit.getDatasourceEncryptionStrategy());

    DatasourceCopier dataCopier = DatasourceCopier.Builder.newCopier().dontCopyMetadata().dontCopyNullValues().withLoggingListener().withThroughtputListener().build();

    MagmaEngine.get().addDatasource(inputDatasource);
    try {
      int currentChunk = 0;
      int i = 1;
      Datasource destination = createOutput(i++, inputFilename, outputDir);
      for(ValueTable source : inputDatasource.getValueTables()) {
        ValueTableWriter writer = dataCopier.createValueTableWriter(source, source.getName(), destination);

        DatasourceCopier.Builder.newCopier().dontCopyValues().build().copy(source, source.getName(), writer);

        for(ValueSet vs : source.getValueSets()) {
          ValueSetWriter vsw = writer.writeValueSet(vs.getVariableEntity());
          dataCopier.copy(source, vs, source.getName(), vsw);
          close(vsw);

          // Increment the chunk counter and test boundary.
          // Split if we've written enough value sets.
          if(++currentChunk >= options.getChunkSize()) {
            // Split boundary
            currentChunk = 0;
            close(writer);

            MagmaEngine.get().removeDatasource(destination);
            destination = createOutput(i++, inputFilename, outputDir);
            // Copy variables
            writer = dataCopier.createValueTableWriter(source, source.getName(), destination);
            DatasourceCopier.Builder.newCopier().dontCopyValues().build().copy(source, source.getName(), writer);
          }
        }
      }
      MagmaEngine.get().removeDatasource(destination);
    } catch(Exception e) {
      throw new RuntimeException(e);
    } finally {
      MagmaEngine.get().removeDatasource(inputDatasource);
    }
  }

  private Datasource createOutput(int i, String inputFilename, FileObject outputDir) {
    try {
      FileObject newFile = outputDir.resolveFile(inputFilename.replace(".zip", "-" + i + ".zip"));
      getShell().printf("  Writing to %s\n", newFile.getName().getPath());
      File localOutputFile = getOpalRuntime().getFileSystem().getLocalFile(newFile);
      return MagmaEngine.get().addDatasource(new FsDatasource(newFile.getName().getBaseName(), localOutputFile));
    } catch(FileSystemException e) {
      throw new RuntimeException(e);
    }
  }

  private void close(ValueTableWriter vtw) {
    try {
      vtw.close();
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void close(ValueSetWriter vsw) {
    try {
      vsw.close();
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

}
