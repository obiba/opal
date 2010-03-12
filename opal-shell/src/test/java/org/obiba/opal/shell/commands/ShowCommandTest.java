/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.shell.commands;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.test.AbstractMagmaTest;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.shell.OpalShell;
import org.obiba.opal.shell.commands.options.ShowCommandOptions;

import com.google.common.collect.ImmutableSet;

/**
 *
 */
public class ShowCommandTest extends AbstractMagmaTest {
  //
  // Fixture Methods (setUp / tearDown)
  //

  @Before
  public void setUp() {
    MagmaEngine.get().addDatasource(createMockDatasource("ds1", "table1", "table2"));
    MagmaEngine.get().addDatasource(createMockDatasource("ds2", "tableA", "tableB"));
  }

  //
  // Test Methods
  //

  @Test
  public void testShowUnits() {
    ShowCommandOptions mockOptions = createMock(ShowCommandOptions.class);
    expect(mockOptions.getDatasources()).andReturn(false).anyTimes();
    expect(mockOptions.getTables()).andReturn(false).anyTimes();
    expect(mockOptions.getUnits()).andReturn(true).anyTimes();

    OpalShell mockShell = createMock(OpalShell.class);
    recordExpectedShellOutputForUnits(mockShell);

    replay(mockOptions, mockShell);

    ShowCommand showCommand = createShowCommand();
    showCommand.setOptions(mockOptions);
    showCommand.setShell(mockShell);
    showCommand.execute();

    verify(mockOptions, mockShell);
  }

  @Test
  public void testShowAll() {
    ShowCommandOptions mockOptions = createMock(ShowCommandOptions.class);
    recordExpectedOperationsOnOptions(mockOptions);

    OpalShell mockShell = createMock(OpalShell.class);
    recordExpectedShellOutputForDatasourcesAndTables(mockShell);
    recordExpectedShellOutputForUnits(mockShell);

    replay(mockOptions, mockShell);

    ShowCommand showCommand = createShowCommand();
    showCommand.setOptions(mockOptions);
    showCommand.setShell(mockShell);
    showCommand.execute();

    verify(mockOptions, mockShell);
  }

  private void recordExpectedOperationsOnOptions(ShowCommandOptions mockOptions) {
    expect(mockOptions.getDatasources()).andReturn(false).anyTimes();
    expect(mockOptions.getTables()).andReturn(false).anyTimes();
    expect(mockOptions.getUnits()).andReturn(false).anyTimes();
  }

  private void recordExpectedShellOutputForUnits(OpalShell mockShell) {
    mockShell.printf("functional unit [%s], with key variable [%s]\n", "unit1", "unit1KeyVariable");
    mockShell.printf("functional unit [%s], with key variable [%s]\n", "unit2", "unit2KeyVariable");
  }

  private void recordExpectedShellOutputForDatasourcesAndTables(OpalShell mockShell) {
    mockShell.printf("%s\n", "ds1");
    mockShell.printf("%s\n", "ds2");
    mockShell.printf("%s.%s\n", "ds1", "table1");
    mockShell.printf("%s.%s\n", "ds1", "table2");
    mockShell.printf("%s.%s\n", "ds2", "tableA");
    mockShell.printf("%s.%s\n", "ds2", "tableB");
  }

  //
  // Methods
  //

  private ShowCommand createShowCommand() {
    return new ShowCommand() {
      @Override
      protected OpalConfiguration getOpalConfiguration() {
        return createOpalConfiguration();
      }
    };
  }

  private OpalConfiguration createOpalConfiguration() {
    OpalConfiguration opalConfiguration = new OpalConfiguration();

    ImmutableSet.Builder<FunctionalUnit> builder = new ImmutableSet.Builder<FunctionalUnit>();
    builder.add(new FunctionalUnit("unit1", "unit1KeyVariable"));
    builder.add(new FunctionalUnit("unit2", "unit2KeyVariable"));

    opalConfiguration.setFunctionalUnits(builder.build());

    return opalConfiguration;
  }

  private Datasource createMockDatasource(String name, String... tables) {
    Datasource mockDatasource = createMock(Datasource.class);
    expect(mockDatasource.getName()).andReturn(name).anyTimes();

    ImmutableSet.Builder<ValueTable> builder = new ImmutableSet.Builder<ValueTable>();
    for(String tableName : tables) {
      builder.add(createMockTable(tableName));
    }
    expect(mockDatasource.getValueTables()).andReturn(builder.build());

    mockDatasource.initialise();
    mockDatasource.dispose();

    replay(mockDatasource);

    return mockDatasource;
  }

  private ValueTable createMockTable(String name) {
    ValueTable mockTable = createMock(ValueTable.class);
    expect(mockTable.getName()).andReturn(name).anyTimes();

    replay(mockTable);

    return mockTable;
  }
}
