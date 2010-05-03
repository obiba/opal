/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.server.httpd;

import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.unit.security.FunctionalUnitRealm;
import org.obiba.opal.shell.commands.AbstractOpalRuntimeDependentCommand;
import org.obiba.opal.shell.commands.CommandUsage;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
@CommandUsage(description = "Grant permissions to functional units.", syntax = "Syntax: grant --unit NAME --perm PERM TABLES...")
public class GrantCommand extends AbstractOpalRuntimeDependentCommand<GrantCommandOptions> {

  @Autowired
  private OpalRuntime opal;

  @Autowired
  private FunctionalUnitRealm realm;

  @Override
  public void execute() {
    for(String table : options.getTables()) {
      MagmaEngineTableResolver.valueOf(table).resolveTable();
      realm.grant(opal.getFunctionalUnit(options.getUnit()), "tables:" + table + ":" + options.getPerm());
    }
  }
}
