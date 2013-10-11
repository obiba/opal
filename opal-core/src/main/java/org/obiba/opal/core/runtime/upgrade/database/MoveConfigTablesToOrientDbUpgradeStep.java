package org.obiba.opal.core.runtime.upgrade.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.domain.unit.UnitKeyStoreState;
import org.obiba.opal.core.runtime.database.DatabaseRegistry;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class MoveConfigTablesToOrientDbUpgradeStep extends AbstractUpgradeStep {

//  private static final Logger log = LoggerFactory.getLogger(MoveConfigTablesToOrientDbUpgradeStep.class);

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Autowired
  private OrientDbService orientDbService;

  @Override
  public void execute(Version currentVersion) {
    JdbcTemplate dataJdbcTemplate = new JdbcTemplate(databaseRegistry.getDataSource("opal-data", null));
    moveUnitKeyStore(dataJdbcTemplate);
    moveSubjectAcl(dataJdbcTemplate);
  }

  private void moveUnitKeyStore(JdbcTemplate dataJdbcTemplate) {
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

  private void moveSubjectAcl(JdbcTemplate dataJdbcTemplate) {
    List<SubjectAcl> list = dataJdbcTemplate.query("select * from subject_acl", new RowMapper<SubjectAcl>() {
      @Override
      public SubjectAcl mapRow(ResultSet rs, int rowNum) throws SQLException {
        SubjectAcl acl = new SubjectAcl();
        acl.setDomain(rs.getString("domain"));
        acl.setNode(rs.getString("node"));
        acl.setPermission(rs.getString("permission"));
        acl.setPrincipal(rs.getString("principal"));
        acl.setType(rs.getString("type"));
        return acl;
      }
    });
    for(SubjectAcl acl : list) {
      orientDbService.save(acl);
    }
    dataJdbcTemplate.execute("drop table subject_acl");
  }
}
