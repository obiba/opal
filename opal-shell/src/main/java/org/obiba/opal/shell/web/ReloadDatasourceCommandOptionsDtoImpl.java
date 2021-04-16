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

import org.obiba.opal.shell.commands.options.ReloadDatasourceCommandOptions;
import org.obiba.opal.web.model.Commands.ReloadDatasourceCommandOptionsDto;

public class ReloadDatasourceCommandOptionsDtoImpl implements ReloadDatasourceCommandOptions {

  private final ReloadDatasourceCommandOptionsDto dto;

  public ReloadDatasourceCommandOptionsDtoImpl(ReloadDatasourceCommandOptionsDto dto) {
    this.dto = dto;
  }

  @Override
  public String getProject() {
    return dto.getProject();
  }

  @Override
  public boolean isHelp() {
    return false;
  }
}
