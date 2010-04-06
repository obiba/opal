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

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.ValueTableWriter.ValueSetWriter;
import org.obiba.magma.datasource.crypt.DatasourceEncryptionStrategy;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.opal.shell.commands.options.SplitCommandOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
@CommandUsage(description = "Splits one or more ZIP files into multiple pieces.", syntax = "Syntax: split --out DIR _FILE_...")
public class SplitCommand extends AbstractOpalRuntimeDependentCommand<SplitCommandOptions> {

  private static final Logger log = LoggerFactory.getLogger(SplitCommand.class);

  //
  // Constants
  //

  public static final String DECRYPT_DATASOURCE_NAME = "decrypt-datasource";

  //
  // Instance Variables
  //
  @Autowired
  private DatasourceEncryptionStrategy dsEncryptionStrategy;

  public void execute() {
    // Ensure that options have been set.
    if(options.getFiles() == null) {
      System.console().printf("No input file(s) specified.\n");
      return;
    }

    File outputDir = options.getOutput();
    if(outputDir.exists() == false) {
      if(outputDir.mkdirs() == false) {
        System.console().printf("Could not create output directory: %s\n", outputDir.getPath());
        return;
      }
    } else if(outputDir.isDirectory() == false) {
      System.console().printf("The output '%s' is not a directory.\n", outputDir.getPath());
      return;
    }

    for(File inputFile : options.getFiles()) {
      if(inputFile.exists() == false) {
        System.console().printf("Skipping non-existant input file %s\n", inputFile.getName());
      } else {
        System.console().printf("Splitting input file %s in chunks of %d entities\n", inputFile.getName(), options.getChunkSize());
        processFile(inputFile, outputDir);
      }
    }
  }

  private void processFile(File inputFile, File outputDir) {
    String inputFilename = inputFile.getName();
    FsDatasource inputDatasource = new FsDatasource(inputFilename, inputFile, dsEncryptionStrategy);

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

  private Datasource createOutput(int i, String inputFilename, File outputDir) {
    File newFile = new File(outputDir, inputFilename.replace(".zip", "-" + i + ".zip"));
    System.console().printf("  Writing to %s\n", newFile.getPath());
    return MagmaEngine.get().addDatasource(new FsDatasource(newFile.getName(), newFile));
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
