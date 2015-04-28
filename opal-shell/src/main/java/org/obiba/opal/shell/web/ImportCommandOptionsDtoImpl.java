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

  private final ImportCommandOptionsDto dto;

  //
  // Constructors
  //

  public ImportCommandOptionsDtoImpl(ImportCommandOptionsDto dto) {
    this.dto = dto;
  }

  //
  // ImportCommandOptions Methods
  //

  @Override
  public boolean isHelp() {
    return false;
  }

  @Override
  public String getUnit() {
    return dto.getIdConfig().getName();
  }

  @Override
  public boolean isUnit() {
    return dto.hasIdConfig();
  }

  @Override
  public String getDestination() {
    return dto.getDestination();
  }

  @Override
  public boolean isArchive() {
    return dto.hasArchive();
  }

  @Override
  public String getArchive() {
    return dto.getArchive();
  }

  @Override
  public boolean isFiles() {
    return dto.getFilesCount() != 0;
  }

  @Override
  public List<String> getFiles() {
    return dto.getFilesList();
  }

  @Override
  public String getSource() {
    return dto.getSource();
  }

  @Override
  public boolean isSource() {
    return dto.hasSource();
  }

  @Override
  public List<String> getTables() {
    return dto.getTablesList();
  }

  @Override
  public boolean isTables() {
    return dto.getTablesCount() != 0;
  }

  @Override
  public boolean isForce() {
    return dto.hasIdConfig() && dto.getIdConfig().hasAllowIdentifierGeneration() &&
        dto.getIdConfig().getAllowIdentifierGeneration();
  }

  @Override
  public boolean isIgnore() {
    return dto.hasIdConfig() && dto.getIdConfig().hasIgnoreUnknownIdentifier() &&
        dto.getIdConfig().getIgnoreUnknownIdentifier();
  }

  @Override
  public boolean isIncremental() {
    return dto.getIncremental();
  }

  @Override
  public boolean isCreateVariables() {
    return dto.getCreateVariables();
  }
}
