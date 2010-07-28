/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.shell.web;

import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
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

  private OpalRuntime opalRuntime;

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

  public boolean isHelp() {
    return false;
  }

  public boolean isUnit() {
    return dto.hasUnit();
  }

  public String getUnit() {
    return dto.getUnit();
  }

  public boolean isSource() {
    return dto.hasSource();
  }

  public String getSource() {
    return dto.getSource();
  }

  public boolean isDestination() {
    return dto.hasDestination();
  }

  public String getDestination() {
    return dto.getDestination();
  }

  public boolean isOut() {
    return dto.hasOut();
  }

  public String getOut() {
    return pathWithExtension != null ? pathWithExtension : dto.getOut();
  }

  public boolean getNonIncremental() {
    return dto.getNonIncremental();
  }

  public boolean getNoValues() {
    return dto.getNoValues();
  }

  public boolean getNoVariables() {
    return dto.getNoVariables();
  }

  public boolean isMultiplex() {
    return dto.hasMultiplex();
  }

  public String getMultiplex() {
    return dto.getMultiplex();
  }

  public boolean isTransform() {
    return dto.hasTransform();
  }

  public String getTransform() {
    return dto.getTransform();
  }

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

      if(file.getType() == FileType.FILE) {
        if(outputFileFormat.equals("csv") && !outputFilePath.endsWith(".csv")) {
          modifiedPath = outputFilePath + ".csv";
        } else if(outputFileFormat.equals("excel") && !outputFilePath.endsWith(".xls") && !outputFilePath.endsWith(".xlsx")) {
          modifiedPath = outputFilePath + ".xlsx"; // prefer .xlsx over .xls
        } else if(outputFileFormat.equals("xml") && !outputFilePath.endsWith(".zip")) {
          modifiedPath = outputFilePath + ".zip";
        }
      }
    } catch(FileSystemException ex) {
      log.error("Unexpected file system exception in addFileExtensionIfMissing", ex);
    }

    return modifiedPath;
  }
}
