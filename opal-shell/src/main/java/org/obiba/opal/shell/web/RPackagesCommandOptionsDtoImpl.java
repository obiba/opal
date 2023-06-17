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

import org.obiba.opal.shell.commands.options.RPackagesCommandOptions;
import org.obiba.opal.web.model.Commands;

public class RPackagesCommandOptionsDtoImpl implements RPackagesCommandOptions {

  private final Commands.RPackagesCommandOptionsDto dto;

  public RPackagesCommandOptionsDtoImpl(Commands.RPackagesCommandOptionsDto dto) {
    this.dto = dto;
  }

  @Override
  public boolean isHelp() {
    return false;
  }

  @Override
  public String getRCluster() {
    return dto.getCluster();
  }
}
