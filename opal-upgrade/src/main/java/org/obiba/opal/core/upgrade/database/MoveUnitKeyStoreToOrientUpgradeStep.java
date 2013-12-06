package org.obiba.opal.core.upgrade.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.obiba.opal.core.domain.unit.KeyStoreState;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@SuppressWarnings("SpringJavaAutowiringInspection")
public class MoveUnitKeyStoreToOrientUpgradeStep extends AbstractUpgradeStep {

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Autowired
  private OrientDbService orientDbService;

  @Override
  public void execute(Version currentVersion) {

    orientDbService.createUniqueIndex(KeyStoreState.class);

    JdbcTemplate dataJdbcTemplate = new JdbcTemplate(databaseRegistry.getDataSource("opal-data", null));
    List<KeyStoreState> states = dataJdbcTemplate.query("select * from unit_key_store", new RowMapper<KeyStoreState>() {
      @Override
      public KeyStoreState mapRow(ResultSet rs, int rowNum) throws SQLException {
        KeyStoreState state = new KeyStoreState();
        state.setName(rs.getString("unit"));
        state.setKeyStore(rs.getBytes("key_store"));
        return state;
      }
    });
    for(KeyStoreState state : states) {
      orientDbService.save(null, state);
    }
    dataJdbcTemplate.execute("drop table unit_key_store");
  }

}
