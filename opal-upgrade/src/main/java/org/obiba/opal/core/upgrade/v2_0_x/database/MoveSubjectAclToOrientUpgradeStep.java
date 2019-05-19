/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.upgrade.v2_0_x.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;

public class MoveSubjectAclToOrientUpgradeStep extends AbstractUpgradeStep {

  private static final Logger log = LoggerFactory.getLogger(MoveSubjectAclToOrientUpgradeStep.class);

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
        try {
        orientDbService.save(null, acl);
        } catch(Exception e) {
          log.error("Unable to save SubjectAcl: {}", acl, e);
        }
      }
    }
    dataJdbcTemplate.execute("drop table subject_acl");
  }

  private static class SubjectAclRowMapper implements RowMapper<SubjectAcl> {
    @Override
    public SubjectAcl mapRow(ResultSet rs, int rowNum) throws SQLException {
      SubjectAcl acl = new SubjectAcl();
      acl.setDomain(upgradeDomain(rs.getString("domain")));
      acl.setNode(upgradeNode(rs.getString("node")));
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

    private String upgradeNode(String node) {
      switch(node) {
        case "/datashield/session":
          return "/datashield";
        case "/r/session":
          return "/r";
        default:
          return node;
      }
    }

    @SuppressWarnings({ "PMD.NcssMethodCount", "OverlyLongMethod" })
    private String upgradePermission(String permission) {
      switch(permission) {
        case "CREATE_TABLE":
          return "TABLE_ADD";
        case "CREATE_VIEW":
          return "TABLE_ADD";
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
        case "R_SESSION_ALL":
          return "R_USE";
        case "DATASHIELD_SESSION_ALL":
          return "DATASHIELD_USE";
        default:
          return permission;
      }
    }

    private String upgradeDomain(String domain) {
      return "magma".equals(domain) ? "rest" : domain;
    }
  }

}
