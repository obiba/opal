/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.database;

import org.junit.Test;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.SqlSettings;
import org.obiba.opal.web.model.Database.DatabaseDto;

import static org.fest.assertions.api.Assertions.assertThat;

public class DatabaseDtosTest {

  @Test
  public void test_a_project_owned_database_says_whose_it_is() {
    DatabaseDto dto = Dtos.asDto(projectOwned("CLSA"), false, true, true);

    assertThat(dto.getName()).isEqualTo("_project_CLSA");
    assertThat(dto.getOwnerProject()).isEqualTo("CLSA");
    assertThat(dto.getOwnerProjectExists()).isTrue();
  }

  /**
   * What the administration page has to know is not who owns the row but whether it may act on it: a leftover whose
   * project is gone is an operator's to remove.
   */
  @Test
  public void test_a_leftover_says_its_project_is_gone() {
    DatabaseDto dto = Dtos.asDto(projectOwned("CLSA"), false, true, false);

    assertThat(dto.getOwnerProject()).isEqualTo("CLSA");
    assertThat(dto.getOwnerProjectExists()).isFalse();
  }

  @Test
  public void test_a_registered_database_has_no_owner() {
    Database database = projectOwned("CLSA");
    database.setName("opal-data");
    database.setOwnerProject(null);

    DatabaseDto dto = Dtos.asDto(database, false, true, true);

    assertThat(dto.hasOwnerProject()).isFalse();
    assertThat(dto.getOwnerProjectExists()).isFalse();
  }

  /**
   * The password of a project-owned database is the one field an operator has no use for and cannot change - and no
   * database's password is sent, which is what this pins.
   */
  @Test
  public void test_no_password_is_sent() {
    DatabaseDto dto = Dtos.asDto(projectOwned("CLSA"), false, true, true);

    assertThat(dto.getSqlSettings().hasPassword()).isFalse();
    assertThat(dto.getSqlSettings().getUrl()).isEqualTo("jdbc:h2:file:CLSA/data");
    assertThat(dto.getSqlSettings().getUsername()).isEqualTo("opal");
  }

  /**
   * Ownership is never set from a payload.
   */
  @Test
  public void test_the_mapping_back_ignores_ownership() {
    DatabaseDto dto = Dtos.asDto(projectOwned("CLSA"), false, true, true);

    assertThat(Dtos.fromDto(dto).getOwnerProject()).isNull();
    assertThat(Dtos.fromDto(dto).isProjectOwned()).isFalse();
  }

  private Database projectOwned(String projectName) {
    return Database.Builder.create() //
        .name("_project_" + projectName) //
        .ownerProject(projectName) //
        .usage(Database.Usage.STORAGE) //
        .defaultStorage(false) //
        .usedForIdentifiers(false) //
        .sqlSettings(SqlSettings.Builder.create() //
            .sqlSchema(SqlSettings.SqlSchema.JDBC) //
            .driverClass("org.h2.Driver") //
            .url("jdbc:h2:file:" + projectName + "/data") //
            .username("opal") //
            .password("a-generated-password")) //
        .build();
  }
}
