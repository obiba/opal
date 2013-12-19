package org.obiba.opal.core.upgrade.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;

public class MoveSubjectAclToOrientUpgradeStep extends AbstractUpgradeStep {

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Autowired
  private OrientDbService orientDbService;

  @Override
  public void execute(Version currentVersion) {
    orientDbService.createUniqueIndex(SubjectAcl.class);
    orientDbService.createIndex(SubjectAcl.class, OClass.INDEX_TYPE.NOTUNIQUE, OType.STRING, "domain");
    orientDbService.createIndex(SubjectAcl.class, OClass.INDEX_TYPE.NOTUNIQUE, OType.STRING, "node");
    orientDbService.createIndex(SubjectAcl.class, OClass.INDEX_TYPE.NOTUNIQUE, OType.STRING, "principal");
    orientDbService.createIndex(SubjectAcl.class, OClass.INDEX_TYPE.NOTUNIQUE, OType.STRING, "type");

    JdbcTemplate dataJdbcTemplate = new JdbcTemplate(databaseRegistry.getDataSource("opal-data", null));
    List<SubjectAcl> list = dataJdbcTemplate.query("select * from subject_acl", new SubjectAclRowMapper());
    for(SubjectAcl acl : list) {
      if(!acl.getNode().startsWith("/auth/session/") && !"FILES_META".equals(acl.getPermission())) {
        orientDbService.save(null, acl);
      }
    }
    dataJdbcTemplate.execute("drop table subject_acl");
  }

  private static class SubjectAclRowMapper implements RowMapper<SubjectAcl> {
    @Override
    public SubjectAcl mapRow(ResultSet rs, int rowNum) throws SQLException {
      SubjectAcl acl = new SubjectAcl();
      acl.setDomain(upgradeDomain(rs.getString("domain")));
      acl.setNode(rs.getString("node"));
      acl.setPermission(upgradePermission(rs.getString("permission")));
      acl.setPrincipal(rs.getString("principal"));
      switch(rs.getString("type")) {
        case "USER":
          acl.setType(SubjectAcl.SubjectType.USER);
          break;
        case "GROUP":
          acl.setType(SubjectAcl.SubjectType.GROUP);
          break;
      }
      return acl;
    }

    private String upgradePermission(String permission) {
      switch(permission) {
        case "CREATE_VIEW":
          return "CREATE_TABLE";
        case "VIEW_ALL":
          return "TABLE_ALL";
        case "VIEW_READ":
          return "TABLE_READ";
        case "VIEW_VALUES":
          return "TABLE_VALUES";
        case "VIEW_EDIT":
          return "TABLE_EDIT";
        case "VIEW_VALUES_EDIT":
          return "TABLE_VALUES_EDIT";
        default:
          return permission;
      }
    }

    private String upgradeDomain(String domain) {
      return "magma".equals(domain) ? "rest" : domain;
    }
  }

}
