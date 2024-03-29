/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.upgrade;

import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.UpgradeStep;
import org.w3c.dom.Document;

public abstract class AbstractConfigurationUpgradeStep extends AbstractConfigurationAlteringStep
    implements UpgradeStep {

  @Override
  public void execute(Version currentVersion) {
    Document opalConfig = getOpalConfigurationAsDocument();
    doWithConfig(opalConfig);
    writeOpalConfig(opalConfig);
  }

  protected abstract void doWithConfig(Document opalConfig);
}
