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

import org.junit.Test;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.runtime.IOpalRuntime;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.shell.OpalShell;
import org.obiba.opal.shell.commands.options.ImportCommandOptions;

import com.google.common.collect.ImmutableSet;

/**
 * Unit tests for {@link ImportCommand}.
 */
public class ImportCommandTest {
  //
  // Test Methods
  //

  @Test
  public void testImportIntoOpalInstanceNotAllowed() {
    testImportIntoNonExistingUnit(FunctionalUnit.OPAL_INSTANCE);
  }

  @Test
  public void testImportIntoBogusUnitNotAllowed() {
    testImportIntoNonExistingUnit("bogus");
  }

  //
  // Methods
  //

  private ImportCommand createImportCommand(final IOpalRuntime mockRuntime) {
    return new ImportCommand() {
      @Override
      protected IOpalRuntime getOpalRuntime() {
        return mockRuntime;
      }

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

    opalConfiguration.setFunctionalUnits(builder.build());

    return opalConfiguration;
  }

  private void testImportIntoNonExistingUnit(String unitName) {
    ImportCommandOptions mockOptions = createMock(ImportCommandOptions.class);
    expect(mockOptions.getUnit()).andReturn(unitName).anyTimes();

    OpalShell mockShell = createMock(OpalShell.class);
    mockShell.printf("Functional unit '%s' does not exist. Cannot decrypt.\n", unitName);

    IOpalRuntime mockRuntime = createMock(IOpalRuntime.class);
    expect(mockRuntime.getFunctionalUnit(unitName)).andReturn(null);

    replay(mockOptions, mockShell, mockRuntime);

    ImportCommand importCommand = createImportCommand(mockRuntime);
    importCommand.setOptions(mockOptions);
    importCommand.setShell(mockShell);
    importCommand.execute();

    verify(mockOptions, mockShell, mockRuntime);
  }

}
