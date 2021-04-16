/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.shell.web;

import org.obiba.opal.shell.commands.options.RestoreCommandOptions;
import org.obiba.opal.web.model.Commands;

public class RestoreCommandOptionsDtoImpl implements RestoreCommandOptions {

  private final String project;

  private final Commands.RestoreCommandOptionsDto dto;

  public RestoreCommandOptionsDtoImpl(String project, Commands.RestoreCommandOptionsDto dto) {
    this.project = project;
    this.dto = dto;
  }

  @Override
  public String getProject() {
    return project;
  }

  @Override
  public String getArchive() {
    return dto.getArchive();
  }

  @Override
  public String getPassword() {
    return isPassword() ? dto.getPassword() : null;
  }

  @Override
  public boolean isPassword() {
    return dto.hasPassword();
  }

  @Override
  public boolean getOverride() {
    return isOverride() && dto.getOverride();
  }

  @Override
  public boolean isOverride() {
    return dto.hasOverride();
  }

  @Override
  public boolean isHelp() {
    return false;
  }
}
