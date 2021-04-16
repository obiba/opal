/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.upgrade.v2_0_x.database;

import org.obiba.opal.core.upgrade.AbstractConfigurationUpgradeStep;
import org.obiba.runtime.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class CopyVersionToOpalConfigurationUpgradeStep extends AbstractConfigurationUpgradeStep {

  /*
    <version>
      <major>2</major>
      <minor>0</minor>
      <micro>0</micro>
      <qualifier />
    </version>
  */
  @Override
  protected void doWithConfig(Document opalConfig) {

    Node major = opalConfig.createElement("major");
    major.setTextContent("2");
    Node minor = opalConfig.createElement("minor");
    minor.setTextContent("0");
    Node micro = opalConfig.createElement("micro");
    micro.setTextContent("0");
    Node qualifier = opalConfig.createElement("qualifier");
    qualifier.setTextContent("");

    Node version = opalConfig.createElement("version");
    version.appendChild(major);
    version.appendChild(minor);
    version.appendChild(micro);
    version.appendChild(qualifier);

    opalConfig.getFirstChild().appendChild(version);
  }

  @Override
  @SuppressWarnings("MethodReturnAlwaysConstant")
  public String getDescription() {
    return "Drop version table from previous opal-data database because it's now moved to opal-config database";
  }

  @Override
  public Version getAppliesTo() {
    return new Version(2, 0, 0);
  }
}
