/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
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

  private final Commands.RestoreCommandOptionsDto dto;

  public RestoreCommandOptionsDtoImpl(Commands.RestoreCommandOptionsDto dto) {
    this.dto = dto;
  }

  @Override
  public String getProject() {
    return dto.getProject();
  }

  @Override
  public String getArchive() {
    return dto.getArchive();
  }

  @Override
  public String getPassword() {
    return dto.getPassword();
  }

  @Override
  public boolean isPassword() {
    return dto.hasPassword();
  }

  @Override
  public boolean isHelp() {
    return false;
  }
}
