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

import org.obiba.opal.shell.commands.options.FileBundleCommandOptions;
import org.obiba.opal.web.model.Commands;

public class FileBundleCommandOptionsDtoImpl implements FileBundleCommandOptions {

  private final Commands.FileBundleCommandOptionsDto dto;

  public FileBundleCommandOptionsDtoImpl(Commands.FileBundleCommandOptionsDto dto) {
    this.dto = dto;
  }

  @Override
  public String getPath() {
    return dto.getPath();
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
  public boolean isHelp() {
    return false;
  }
}

