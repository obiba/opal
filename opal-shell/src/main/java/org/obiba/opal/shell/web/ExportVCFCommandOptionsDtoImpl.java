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

import com.google.common.base.Strings;
import org.obiba.opal.shell.commands.options.ExportVCFCommandOptions;
import org.obiba.opal.web.model.Commands;

import java.util.List;

public class ExportVCFCommandOptionsDtoImpl implements ExportVCFCommandOptions {

  private final Commands.ExportVCFCommandOptionsDto options;

  public ExportVCFCommandOptionsDtoImpl(Commands.ExportVCFCommandOptionsDto options) {
    this.options = options;
  }

  @Override
  public boolean isHelp() {
    return false;
  }

  @Override
  public List<String> getNames() {
    return options.getNamesList();
  }

  @Override
  public String getProject() {
    return options.getProject();
  }

  @Override
  public String getDestination() {
    return options.getDestination();
  }

  @Override
  public String getTable() {
    return hasTable() ? options.getTable() : "";
  }

  @Override
  public boolean hasTable() {
    return options.hasTable() && !Strings.isNullOrEmpty(options.getTable());
  }

  @Override
  public boolean isCaseControl() {
    return options.hasCaseControl() ? options.getCaseControl() : true;
  }

  @Override
  public String getParticipantIdentifiersMapping() {
    return options.getParticipantIdentifiersMapping();
  }

  @Override
  public boolean hasParticipantIdentifiersMapping() {
    return options.hasParticipantIdentifiersMapping() && !Strings.isNullOrEmpty(options.getParticipantIdentifiersMapping());
  }
}
