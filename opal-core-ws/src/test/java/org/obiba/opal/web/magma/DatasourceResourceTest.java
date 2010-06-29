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

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.web.model.Magma;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class DatasourceResourceTest extends MagmaResourceTest {

  private static final Logger log = LoggerFactory.getLogger(DatasourceResourceTest.class);

  @Before
  public void before() {
    super.before();
    addAllDatasources();
  }

  @Test
  public void testDatasourcesGET() {
    DatasourcesResource resource = new DatasourcesResource("opal-keys.keys");

    List<Magma.DatasourceDto> dtos = resource.getDatasources();
    Assert.assertEquals(2, dtos.size());
    Assert.assertEquals(DATASOURCE1, dtos.get(0).getName());
    Assert.assertEquals(DATASOURCE2, dtos.get(1).getName());
  }

  @Test
  public void testDatasourceGET() {
    DatasourceResource resource = new DatasourceResource(DATASOURCE1);

    Magma.DatasourceDto dto = resource.get();

    Assert.assertNotNull(dto);
    Assert.assertEquals(DATASOURCE1, dto.getName());
    List<String> tableNames = dto.getTableList();
    Assert.assertEquals(2, tableNames.size());
    Assert.assertEquals("CIPreliminaryQuestionnaire", tableNames.get(0));
    Assert.assertEquals("StandingHeight", tableNames.get(1));
  }

}
