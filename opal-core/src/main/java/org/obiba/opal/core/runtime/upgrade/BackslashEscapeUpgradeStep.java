/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.runtime.upgrade;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.obiba.magma.NoSuchValueTableException;
import org.obiba.opal.core.runtime.upgrade.support.UpgradeUtils;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import static org.obiba.magma.type.TextType.BACKSLASH;
import static org.obiba.magma.type.TextType.DOUBLE_BACKSLASH;

/**
 *
 */
public class BackslashEscapeUpgradeStep extends AbstractUpgradeStep {

  private static final Logger log = LoggerFactory.getLogger(BackslashEscapeUpgradeStep.class);

  private UpgradeUtils upgradeUtils;

  @Override
  public void execute(Version currentVersion) {

    Map<DataSource, String> configuredDatasources = upgradeUtils.getConfiguredDatasources();

    for(Map.Entry<DataSource, String> entry : configuredDatasources.entrySet()) {
      DataSource dataSource = entry.getKey();
      String dataSourceName = entry.getValue();
      if(UpgradeUtils.hasHibernateDatasource(dataSource)) {
        processDatasource(dataSource, dataSourceName);
      }
    }
  }

  private void processDatasource(DataSource dataSource, final String dataSourceName) {
    final JdbcTemplate template = new JdbcTemplate(dataSource);

    template.query("SELECT variable_id, value_set_id, is_sequence, `value` FROM value_set_value WHERE value_type = ?",
        new Object[] { "text" }, new RowCallbackHandler() {

      @Override
      public void processRow(ResultSet rs) throws SQLException, NoSuchValueTableException {
        String value = rs.getString("value");
        if(value.indexOf('\\') < 0) return;

        int valueSetId = rs.getInt("value_set_id");
        int variableId = rs.getInt("variable_id");

        log.debug("Process dataSource: {} => value_set_id: {}, variable_id: {}", dataSourceName, valueSetId,
            variableId);
        log.debug("  before: {}", value);
        value = value.replaceAll(BACKSLASH, DOUBLE_BACKSLASH);
        if(rs.getBoolean("is_sequence")) {
          value = value.replaceAll(BACKSLASH, DOUBLE_BACKSLASH);
        }
        log.debug("  after: {}", value);

        template.update("UPDATE value_set_value SET `value` = ? WHERE variable_id = ? AND value_set_id =?", value,
            variableId, valueSetId);
      }
    });
  }

  public void setUpgradeUtils(UpgradeUtils upgradeUtils) {
    this.upgradeUtils = upgradeUtils;
  }
}
