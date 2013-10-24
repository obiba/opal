package org.obiba.opal.core.upgrade.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.obiba.opal.core.domain.unit.UnitKeyStoreState;
import org.obiba.opal.core.runtime.database.DatabaseRegistry;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@SuppressWarnings("SpringJavaAutowiringInspection")
public class MoveUnitKeyStoreToOrientDbUpgradeStep extends AbstractUpgradeStep {

//  private static final Logger log = LoggerFactory.getLogger(MoveConfigTablesToOrientDbUpgradeStep.class);

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Autowired
  private OrientDbService orientDbService;

  @Override
  public void execute(Version currentVersion) {
    JdbcTemplate dataJdbcTemplate = new JdbcTemplate(databaseRegistry.getDataSource("opal-data", null));
    List<UnitKeyStoreState> states = dataJdbcTemplate
        .query("select * from unit_key_store", new RowMapper<UnitKeyStoreState>() {
          @Override
          public UnitKeyStoreState mapRow(ResultSet rs, int rowNum) throws SQLException {
            UnitKeyStoreState state = new UnitKeyStoreState();
            state.setUnit(rs.getString("unit"));
            state.setKeyStore(rs.getBytes("key_store"));
            return state;
          }
        });
    for(UnitKeyStoreState state : states) {
      orientDbService.save(state);
    }
    dataJdbcTemplate.execute("drop table unit_key_store");
  }

}
