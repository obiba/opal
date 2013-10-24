package org.obiba.opal.core.upgrade.database;

import org.obiba.opal.core.runtime.database.DatabaseRegistry;
import org.obiba.opal.core.upgrade.AbstractConfigurationUpgradeStep;
import org.obiba.runtime.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

@SuppressWarnings("SpringJavaAutowiringInspection")
public class MoveVersionToOpalConfigurationUpgradeStep extends AbstractConfigurationUpgradeStep {

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Override
  public void execute(Version currentVersion) {
    super.execute(currentVersion);
    JdbcTemplate dataJdbcTemplate = new JdbcTemplate(databaseRegistry.getDataSource("opal-data", null));
    dataJdbcTemplate.execute("drop table version");
  }

  @Override
  protected void doWithConfig(Document opalConfig) {

//    <version>
//    <major>2</major>
//    <minor>0</minor>
//    <micro>0</micro>
//    <qualifier>SNAPSHOT-b20131024143850</qualifier>
//    </version>

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
  public String getDescription() {
    return "Drop version table from previous opal-data database because it's now moved to opal-config database";
  }

  @Override
  public Version getAppliesTo() {
    return new Version(2, 0, 0);
  }
}
