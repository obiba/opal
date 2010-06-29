/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma;

import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class TableResourceTest extends MagmaResourceTest {

  private static final Logger log = LoggerFactory.getLogger(TableResourceTest.class);

  @Before
  public void before() {
    super.before();
    addDatasource(DATASOURCE1);
  }

  @Test
  public void testTablesGET() {
    TablesResource resource = new TablesResource(DATASOURCE1);

    // bug with excel datasource
    // List<Magma.TableDto> dtos = resource.getTables();
    // Assert.assertEquals(2, dtos.size());
    // Assert.assertEquals("CIPreliminaryQuestionnaire", dtos.get(0).getName());
    // Assert.assertEquals("StandingHeight", dtos.get(1).getName());
  }

  @Test
  public void testTableGET() {
    Datasource datasource = MagmaEngine.get().getDatasource(DATASOURCE1);
    TableResource resource = new TableResource(datasource.getValueTable("CIPreliminaryQuestionnaire"));

    // bug with excel datasource
    // Magma.TableDto dto = resource.get(null);
    // Assert.assertNotNull(dto);
  }
}
