/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.runtime.upgrade;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import javax.sql.DataSource;

import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class IdentifiersSyncUpgradeStep extends AbstractUpgradeStep {

  private static final Logger log = LoggerFactory.getLogger(IdentifiersSyncUpgradeStep.class);

  private DataSource opalDataSource;

  private DataSource keyDataSource;

  private String tableReference;

  @Override
  public void execute(Version currentVersion) {
    SimpleJdbcTemplate dataTemplate = new SimpleJdbcTemplate(opalDataSource);
    SimpleJdbcTemplate keyTemplate = new SimpleJdbcTemplate(keyDataSource);

    // get variable entities from both databases
    log.info("Extracting entities from opal-data and opal-key databases...");
    String sql = "select * from variable_entity";
    List<VariableEntityStateDao> dataEntities = dataTemplate.query(sql, new VariableEntityMapper());
    final List<VariableEntityStateDao> keyEntities = keyTemplate.query(sql, new VariableEntityMapper());
    log.info("Found {} entities in opal-data and {} entities in opal-key.", dataEntities.size(), keyEntities.size());

    log.info("Looking for missing entities in opal-key database...");
    insertMissingEntities(keyTemplate, Iterables.filter(dataEntities, new Predicate<VariableEntityStateDao>() {

      @Override
      public boolean apply(VariableEntityStateDao input) {
        for(VariableEntityStateDao keyEntity : keyEntities) {
          if(keyEntity.equals(input)) {
            return false;
          }
        }
        return true;
      }
    }));
  }

  private void insertMissingEntities(SimpleJdbcTemplate keyTemplate, Iterable<VariableEntityStateDao> missingEntities) {
    List<SqlParameterSource> parameters = ImmutableList.<SqlParameterSource>builder()
        .addAll(Iterables.transform(missingEntities, new Function<VariableEntityStateDao, SqlParameterSource>() {

          @Override
          public SqlParameterSource apply(VariableEntityStateDao input) {
            return new BeanPropertySqlParameterSource(input);
          }

        })).build();

    if(parameters.size() > 0) {
      log.info("Inserting {} entities in opal-key database...", parameters.size());
      String sql
          = "insert into variable_entity (type,identifier,created,updated) values (:type,:identifier,:created,:updated)";
      keyTemplate.batchUpdate(sql, parameters.toArray(new SqlParameterSource[parameters.size()]));

      insertMissingEntitiesValueSets(keyTemplate);
    }
  }

  private void insertMissingEntitiesValueSets(SimpleJdbcTemplate keyTemplate) {
    log.info("Looking for id of value table referenced by '{}'...", tableReference);
    MagmaEngineTableResolver tableResolver = MagmaEngineTableResolver.valueOf(tableReference);

    // get the keys value table id
    String sql = "select id from value_table where name=?";
    Long tableId = keyTemplate.queryForObject(sql, Long.class, tableResolver.getTableName());

    if(tableId != null) {
      // join the missing entities to keys value table value sets
      log.info("Looking for missing value sets in opal-key database...");
      sql
          = "select * from variable_entity where id not in (select vs.variable_entity_id from value_set vs where vs.value_table_id=?)";
      List<VariableEntityStateDao> missingEntities = keyTemplate.query(sql, new VariableEntityMapper(), tableId);

      if(missingEntities.size() > 0) {
        List<SqlParameterSource> parameters = ImmutableList.<SqlParameterSource>builder()
            .addAll(Iterables.transform(missingEntities, new Function<VariableEntityStateDao, SqlParameterSource>() {

              @Override
              public SqlParameterSource apply(VariableEntityStateDao input) {
                return new BeanPropertySqlParameterSource(input);
              }

            })).build();

        log.info("Inserting {} value sets for missing entities in opal-key database...", parameters.size());
        sql = "insert into value_set (created,updated,value_table_id,variable_entity_id) values (:created,:updated," +
            tableId + ",:id)";
        keyTemplate.batchUpdate(sql, parameters.toArray(new SqlParameterSource[parameters.size()]));
      }
    }
  }

  public void setOpalDataSource(DataSource opalDataSource) {
    this.opalDataSource = opalDataSource;
  }

  public void setKeyDataSource(DataSource keyDataSource) {
    this.keyDataSource = keyDataSource;
  }

  public void setTableReference(String tableReference) {
    this.tableReference = tableReference;
  }

  public static final class VariableEntityMapper implements RowMapper<VariableEntityStateDao> {

    @Override
    public VariableEntityStateDao mapRow(ResultSet rs, int rowNum) throws SQLException {
      return new VariableEntityStateDao(rs.getLong("id"), rs.getString("type"), rs.getString("identifier"),
          rs.getTimestamp("created"), rs.getTimestamp("updated"));
    }

  }

  public static final class VariableEntityStateDao extends VariableEntityBean {

    private final long id;

    private final Timestamp created;

    private final Timestamp updated;

    public VariableEntityStateDao(long id, String entityType, String entityIdentifier, Timestamp created,
        Timestamp updated) {
      super(entityType, entityIdentifier);
      this.id = id;
      this.created = created;
      this.updated = updated;
    }

    public long getId() {
      return id;
    }

    public Timestamp getCreated() {
      return created;
    }

    public Timestamp getUpdated() {
      return updated;
    }

  }

}
