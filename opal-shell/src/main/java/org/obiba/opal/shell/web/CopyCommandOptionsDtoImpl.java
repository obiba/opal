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

import org.obiba.opal.shell.commands.options.CopyCommandOptions;
import org.obiba.opal.web.model.Commands.CopyCommandOptionsDto;

import com.google.common.base.Strings;

/**
 * Implementation of {@link CopyCommandOptions} based on an instance of {@link CopyCommandOptionsDto}.
 */
public class CopyCommandOptionsDtoImpl implements CopyCommandOptions {

  //
  // Instance Variables
  //

  protected final CopyCommandOptionsDto dto;

  //
  // Constructors
  //

  public CopyCommandOptionsDtoImpl(CopyCommandOptionsDto dto) {
    this.dto = dto;
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
    return false;
  }

  @Override
  public String getUnit() {
    return null;
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
    return false;
  }

  @Override
  public String getOut() {
    return null;
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
    return dto.hasQuery() && !Strings.isNullOrEmpty(dto.getQuery());
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

}
