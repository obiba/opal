/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.runtime.jdbc;

import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.jdbc.JdbcDatasource;
import org.obiba.magma.datasource.jdbc.JdbcDatasourceSettings;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.TextType;

import java.io.File;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Magma creates the metadata schema of a JDBC datasource with Liquibase, which resolves the dialect from the
 * connection. Opal pins both the H2 driver and the Liquibase version, so this checks the combination Opal actually
 * ships rather than the one Magma was tested with.
 */
public class H2JdbcDatasourceTest {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Before
  public void startMagma() {
    new MagmaEngine();
  }

  @After
  public void stopMagma() {
    MagmaEngine.get().shutdown();
  }

  @Test
  public void test_create_table_in_h2_storage() throws Exception {
    JdbcDatasource datasource = createDatasource();

    ValueTableWriter writer = datasource.createWriter("mytable", "Participant");
    try(ValueTableWriter.VariableWriter variableWriter = writer.writeVariables()) {
      variableWriter.writeVariable(Variable.Builder.newVariable("myvar", TextType.get(), "Participant").build());
    }
    writer.close();

    ValueTable table = datasource.getValueTable("mytable");
    assertThat(table.getEntityType()).isEqualTo("Participant");
    assertThat(table.getVariable("myvar").getValueType()).isEqualTo(TextType.get());
  }

  @Test
  public void test_binary_value_round_trip_in_h2_storage() throws Exception {
    JdbcDatasource datasource = createDatasource();

    byte[] bytes = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
    VariableEntity entity = new VariableEntityBean("Participant", "1");
    try(ValueTableWriter writer = datasource.createWriter("images", "Participant")) {
      try(ValueTableWriter.VariableWriter variableWriter = writer.writeVariables()) {
        variableWriter
            .writeVariable(Variable.Builder.newVariable("myimage", BinaryType.get(), "Participant").build());
      }
      Variable variable = datasource.getValueTable("images").getVariable("myimage");
      try(ValueTableWriter.ValueSetWriter valueSetWriter = writer.writeValueSet(entity)) {
        valueSetWriter.writeValue(variable, BinaryType.get().valueOf(bytes));
      }
    }

    ValueTable table = datasource.getValueTable("images");
    Value value = table.getValue(table.getVariable("myimage"), table.getValueSet(entity));
    assertThat((byte[]) value.getValue()).isEqualTo(bytes);
  }

  /**
   * A project-owned database is one store in a folder named after the project - a folder H2 does not create itself,
   * and a name that may hold a space where a registered database's may not.
   */
  @Test
  public void test_a_project_database_is_a_store_in_the_project_folder() throws Exception {
    File h2Root = temporaryFolder.newFolder("h2");

    writeATable(H2DatabaseUrls.expandProject(H2DatabaseUrls.projectUrl("My Study"), h2Root), "owned");

    assertThat(new File(h2Root, "My Study/data.mv.db").isFile()).isTrue();
  }

  /**
   * A folder {@code CLSA/} coexists with a registered database's file {@code CLSA.mv.db}: that is what lets a project
   * be named whatever an operator called a database.
   */
  @Test
  public void test_a_project_database_and_a_registered_one_of_the_same_name_coexist() throws Exception {
    File h2Root = temporaryFolder.newFolder("h2");

    writeATable(H2DatabaseUrls.expandProject(H2DatabaseUrls.projectUrl("CLSA"), h2Root), "owned");
    writeATable(H2DatabaseUrls.expand("jdbc:h2:file:CLSA", h2Root), "registered");

    assertThat(new File(h2Root, "CLSA/data.mv.db").isFile()).isTrue();
    assertThat(new File(h2Root, "CLSA.mv.db").isFile()).isTrue();
  }

  private void writeATable(String url, String tableName) throws Exception {
    BasicDataSource dataSource = createDataSource(url);
    try {
      JdbcDatasource datasource = createDatasource(dataSource);
      try(ValueTableWriter writer = datasource.createWriter(tableName, "Participant");
          ValueTableWriter.VariableWriter variableWriter = writer.writeVariables()) {
        variableWriter.writeVariable(Variable.Builder.newVariable("myvar", TextType.get(), "Participant").build());
      }
      assertThat(datasource.getValueTable(tableName).getVariable("myvar").getValueType()).isEqualTo(TextType.get());
      datasource.dispose();
    } finally {
      // closing the last connection is what closes the H2 store and writes it to disk
      dataSource.close();
    }
  }

  private JdbcDatasource createDatasource() throws Exception {
    return createDatasource(createDataSource(
        H2DatabaseUrls.expand("jdbc:h2:file:opal", temporaryFolder.newFolder("h2"))));
  }

  private JdbcDatasource createDatasource(BasicDataSource dataSource) {
    JdbcDatasource datasource = new JdbcDatasource("test", dataSource, JdbcDatasourceSettings //
        .newSettings("Participant").useMetadataTables(true).multipleDatasources(true).build());
    Initialisables.initialise(datasource);
    return datasource;
  }

  private BasicDataSource createDataSource(String url) {
    BasicDataSource dataSource = new BasicDataSource();
    dataSource.setDriverClassName(H2DatabaseUrls.DRIVER_CLASS);
    dataSource.setUrl(url);
    dataSource.setUsername("sa");
    dataSource.setPassword("password");
    dataSource.setDefaultAutoCommit(false);
    return dataSource;
  }
}
