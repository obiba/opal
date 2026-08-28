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

  private JdbcDatasource createDatasource() throws Exception {
    JdbcDatasource datasource = new JdbcDatasource("test", createDataSource(), JdbcDatasourceSettings //
        .newSettings("Participant").useMetadataTables(true).multipleDatasources(true).build());
    Initialisables.initialise(datasource);
    return datasource;
  }

  private BasicDataSource createDataSource() throws Exception {
    BasicDataSource dataSource = new BasicDataSource();
    dataSource.setDriverClassName(H2DatabaseUrls.DRIVER_CLASS);
    dataSource.setUrl(H2DatabaseUrls.expand("jdbc:h2:file:opal", temporaryFolder.newFolder("h2")));
    dataSource.setUsername("sa");
    dataSource.setPassword("password");
    dataSource.setDefaultAutoCommit(false);
    return dataSource;
  }
}
