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

import org.obiba.opal.shell.commands.options.CopyCommandOptions;
import org.obiba.opal.web.model.Commands.CopyCommandOptionsDto;

/**
 * Implementation of {@link CopyCommandOptions} based on an instance of {@link CopyCommandOptionsDto}.
 */
public class CopyCommandOptionsDtoImpl implements CopyCommandOptions {
  //
  // Instance Variables
  //

  private CopyCommandOptionsDto dto;

  //
  // Constructors
  //

  public CopyCommandOptionsDtoImpl(CopyCommandOptionsDto dto) {
    this.dto = dto;
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
    return dto.getOut();
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

}
