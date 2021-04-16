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

import org.obiba.opal.shell.commands.options.ImportVCFCommandOptions;
import org.obiba.opal.web.model.Commands;

import java.util.List;

public class ImportVCFCommandOptionsDtoImpl implements ImportVCFCommandOptions {

  private final Commands.ImportVCFCommandOptionsDto options;

  public ImportVCFCommandOptionsDtoImpl(Commands.ImportVCFCommandOptionsDto options) {
    this.options = options;
  }

  @Override
  public boolean isHelp() {
    return false;
  }

  @Override
  public String getProject() {
    return options.getProject();
  }

  @Override
  public List<String> getFiles() {
    return options.getFilesList();
  }
}
