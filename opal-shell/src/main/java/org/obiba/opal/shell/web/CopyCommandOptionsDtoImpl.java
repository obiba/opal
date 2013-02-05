/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.shell.web;

import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.shell.commands.options.CopyCommandOptions;
import org.obiba.opal.web.model.Commands.CopyCommandOptionsDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link CopyCommandOptions} based on an instance of {@link CopyCommandOptionsDto}.
 */
public class CopyCommandOptionsDtoImpl implements CopyCommandOptions {
  //
  // Constants
  //

  private static final Logger log = LoggerFactory.getLogger(CopyCommandOptionsDtoImpl.class);

  //
  // Instance Variables
  //

  protected CopyCommandOptionsDto dto;

  private final OpalRuntime opalRuntime;

  private String pathWithExtension;

  //
  // Constructors
  //

  public CopyCommandOptionsDtoImpl(OpalRuntime opalRuntime, CopyCommandOptionsDto dto) {
    this.opalRuntime = opalRuntime;
    this.dto = dto;

    if(dto.hasOut() && dto.hasFormat()) {
      pathWithExtension = addFileExtensionIfMissing(dto.getOut(), dto.getFormat());
    }
  }

  //
  // CopyCommandOptions Methods
  //

  @Override
  public boolean isHelp() {
    return false;
  }

  @Override
  public boolean isUnit() {
    return dto.hasUnit();
  }

  @Override
  public String getUnit() {
    return dto.getUnit();
  }

  @Override
  public boolean isSource() {
    return dto.hasSource();
  }

  @Override
  public String getSource() {
    return dto.getSource();
  }

  @Override
  public boolean isDestination() {
    return dto.hasDestination();
  }

  @Override
  public String getDestination() {
    return dto.getDestination();
  }

  @Override
  public boolean isOut() {
    return dto.hasOut();
  }

  @Override
  public String getOut() {
    return pathWithExtension != null ? pathWithExtension : dto.getOut();
  }

  @Override
  public boolean getNonIncremental() {
    return dto.getNonIncremental();
  }

  @Override
  public boolean getNoValues() {
    return dto.getNoValues();
  }

  @Override
  public boolean getNoVariables() {
    return dto.getNoVariables();
  }

  @Override
  public boolean isMultiplex() {
    return dto.hasMultiplex();
  }

  @Override
  public String getMultiplex() {
    return dto.getMultiplex();
  }

  @Override
  public boolean isTransform() {
    return dto.hasTransform();
  }

  @Override
  public String getTransform() {
    return dto.getTransform();
  }

  @Override
  public List<String> getTables() {
    return dto.getTablesList();
  }

  //
  // Methods
  //

  FileObject resolveFileInFileSystem(String path) throws FileSystemException {
    return opalRuntime.getFileSystem().getRoot().resolveFile(path);
  }

  private String addFileExtensionIfMissing(String outputFilePath, String outputFileFormat) {
    String modifiedPath = outputFilePath;

    FileObject file = null;
    try {
      file = resolveFileInFileSystem(outputFilePath);

      // Add the extension if the file object is an existing file (FileType.FILE)
      // or a new file (FileType.IMAGINARY). We assume here that any "imaginary" file object
      // is a non-existent folder.
      if(file.getType() == FileType.FILE) {
        if("csv".equals(outputFileFormat) && !outputFilePath.endsWith(".csv")) {
          modifiedPath = outputFilePath + ".csv";
        } else if("excel".equals(outputFileFormat) && !outputFilePath.endsWith(".xls") &&
            !outputFilePath.endsWith(".xlsx")) {
          modifiedPath = outputFilePath + ".xlsx"; // prefer .xlsx over .xls
        } else if("xml".equals(outputFileFormat) && !outputFilePath.endsWith(".zip")) {
          modifiedPath = outputFilePath + ".zip";
        }
      } else if(file.getType() == FileType.IMAGINARY) {
        if("xml".equals(outputFileFormat) && !outputFilePath.endsWith(".zip")) {
          modifiedPath = outputFilePath + ".zip";
        } else if("csv".equals(outputFileFormat)) {
          // Create the directory
          file.createFolder();
        }
      }

    } catch(FileSystemException ex) {
      log.error("Unexpected file system exception in addFileExtensionIfMissing", ex);
    }

    return modifiedPath;
  }
}
