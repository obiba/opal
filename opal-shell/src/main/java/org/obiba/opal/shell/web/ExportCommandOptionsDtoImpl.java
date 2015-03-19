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
import org.obiba.opal.web.model.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * Implementation of {@link org.obiba.opal.shell.commands.options.CopyCommandOptions} based on an instance of {@link org.obiba.opal.web.model.Commands.CopyCommandOptionsDto}.
 */
public class ExportCommandOptionsDtoImpl implements CopyCommandOptions {
  //
  // Constants
  //

  private static final Logger log = LoggerFactory.getLogger(ExportCommandOptionsDtoImpl.class);

  //
  // Instance Variables
  //

  protected final Commands.ExportCommandOptionsDto dto;

  private final OpalRuntime opalRuntime;

  private String pathWithExtension;

  //
  // Constructors
  //

  public ExportCommandOptionsDtoImpl(OpalRuntime opalRuntime, Commands.ExportCommandOptionsDto dto) {
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
    return dto.hasIdConfig();
  }

  @Override
  public String getUnit() {
    return dto.getIdConfig().getName();
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
  public String getName() {
    return dto.getDestinationTableName();
  }

  @Override
  public boolean isName() {
    return dto.hasDestinationTableName();
  }

  @Override
  public String getQuery() {
    return dto.getQuery();
  }

  @Override
  public boolean isQuery() {
    return dto.hasQuery() && !Strings.isNullOrEmpty(dto.getQuery()) && !"*".equals(dto.getQuery());
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
  public boolean getCopyNullValues() {
    return dto.getCopyNullValues();
  }

  @Override
  public boolean isMultiplex() {
    return false;
  }

  @Override
  public String getMultiplex() {
    return null;
  }

  @Override
  public boolean isTransform() {
    return false;
  }

  @Override
  public String getTransform() {
    return null;
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
        modifiedPath = addExtension(outputFileFormat, outputFilePath);

      } else if(file.getType() == FileType.IMAGINARY) {
        if("xml".equals(outputFileFormat) && !outputFilePath.endsWith(".zip")) {
          modifiedPath = addExtension(outputFileFormat, outputFilePath);
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

  private String addExtension(String outputFileFormat, String outputFilePath) {
    if("csv".equals(outputFileFormat) && !outputFilePath.endsWith(".csv")) {
      return outputFilePath + ".csv";
    }
    if("excel".equals(outputFileFormat) && !outputFilePath.endsWith(".xls") &&
        !outputFilePath.endsWith(".xlsx")) {
      return outputFilePath + ".xlsx"; // prefer .xlsx over .xls
    }
    if("xml".equals(outputFileFormat) && !outputFilePath.endsWith(".zip")) {
      return outputFilePath + ".zip";
    }
    return outputFilePath;
  }
}
