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

import java.io.File;
import java.io.IOException;

import org.easymock.EasyMock;
import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.test.AbstractMagmaTest;
import org.obiba.opal.core.service.ExportService;
import org.obiba.opal.shell.OpalShell;
import org.obiba.opal.shell.commands.options.CopyCommandOptions;

import com.google.common.collect.ImmutableList;

/**
 *
 */
public class CopyCommandTest extends AbstractMagmaTest {

  @Test
  public void testMultipleTablesToExcelFile() throws IOException {
    CopyCommand command = new CopyCommand();

    OpalShell mockShell = EasyMock.createMock(OpalShell.class);
    CopyCommandOptions mockOptions = EasyMock.createMock(CopyCommandOptions.class);
    ExportService mockService = EasyMock.createMock(ExportService.class);

    command.setShell(mockShell);
    command.setOptions(mockOptions);
    command.setExportService(mockService);

    mockShell.printf((String) EasyMock.anyObject(), (String) EasyMock.anyObject());
    EasyMock.expectLastCall().anyTimes();
    EasyMock.expect(mockOptions.isSource()).andReturn(false).anyTimes();
    EasyMock.expect(mockOptions.isDestination()).andReturn(false).anyTimes();
    EasyMock.expect(mockOptions.isOut()).andReturn(true).anyTimes();
    File out = File.createTempFile("test", ".xlsx");
    out.delete();
    EasyMock.expect(mockOptions.getOut()).andReturn(out.getName()).anyTimes();
    EasyMock.expect(mockOptions.getTables()).andReturn(ImmutableList.of("ds.table1", "ds.table2")).anyTimes();
    EasyMock.expect(mockOptions.isMultiplex()).andReturn(false).anyTimes();
    EasyMock.expect(mockOptions.isTransform()).andReturn(false).anyTimes();
    EasyMock.expect(mockOptions.getNoValues()).andReturn(false).anyTimes();
    EasyMock.expect(mockOptions.getNonIncremental()).andReturn(true).anyTimes();

    EasyMock.expect(mockService.newCopier((Datasource) EasyMock.anyObject())).andReturn(DatasourceCopier.Builder.newCopier()).anyTimes();

    MagmaEngine.get().addDatasource(createMockDatasource("ds", "table1", "table2"));

    EasyMock.replay(mockShell, mockOptions, mockService);
    command.execute();
    EasyMock.verify(mockShell, mockOptions, mockService);
  }

  private Datasource createMockDatasource(String dsName, String... tables) {
    Datasource mockDatasource = EasyMock.createMock(Datasource.class);
    mockDatasource.initialise();
    EasyMock.expectLastCall().once();
    mockDatasource.dispose();
    EasyMock.expectLastCall().once();

    EasyMock.expect(mockDatasource.getName()).andReturn(dsName).anyTimes();
    for(String table : tables) {
      ValueTable mockTable = EasyMock.createMock(ValueTable.class);
      EasyMock.expect(mockTable.getName()).andReturn(table).anyTimes();
      EasyMock.expect(mockDatasource.getValueTable(table)).andReturn(mockTable).anyTimes();
      EasyMock.replay(mockTable);
    }
    EasyMock.replay(mockDatasource);
    return mockDatasource;
  }
}
