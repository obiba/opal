package org.obiba.opal.core.runtime.upgrade.database;

import org.obiba.opal.core.runtime.upgrade.AbstractConfigurationUpgradeStep;
import org.obiba.runtime.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class UpdateOpalConfigurationFlagUpgradeStep extends AbstractConfigurationUpgradeStep {

  @Override
  protected void doWithConfig(Document opalConfig) {
    Node migratedToOpal2 = opalConfig.createElement("migratedToOpal2");
    migratedToOpal2.setTextContent("true");
    opalConfig.getFirstChild().appendChild(migratedToOpal2);
  }

  @Override
  public String getDescription() {
    return "Update Opal Configuration to indicate we've migrated databases.";
  }

  @Override
  public Version getAppliesTo() {
    return new Version(2, 0, 0);
  }

}
