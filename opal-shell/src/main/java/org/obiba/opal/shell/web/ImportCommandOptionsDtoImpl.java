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

import org.obiba.opal.shell.commands.options.ImportCommandOptions;
import org.obiba.opal.web.model.Commands.ImportCommandOptionsDto;

/**
 * Implementation of {@link ImportCommandOptions} based on an instance of {@link ImportCommandOptionsDto}.
 */
public class ImportCommandOptionsDtoImpl implements ImportCommandOptions {
  //
  // Instance Variables
  //

  private ImportCommandOptionsDto dto;

  //
  // Constructors
  //

  public ImportCommandOptionsDtoImpl(ImportCommandOptionsDto dto) {
    this.dto = dto;
  }

  //
  // ImportCommandOptions Methods
  //

  public boolean isHelp() {
    return false;
  }

  public String getUnit() {
    return dto.getUnit();
  }

  public String getDestination() {
    return dto.getDestination();
  }

  public boolean isArchive() {
    return dto.hasArchive();
  }

  public String getArchive() {
    return dto.getArchive();
  }

  public boolean isFiles() {
    return dto.getFilesCount() != 0;
  }

  public List<String> getFiles() {
    return dto.getFilesList();
  }
}
