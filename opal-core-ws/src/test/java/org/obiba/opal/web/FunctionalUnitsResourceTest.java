/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.UnitKeyStoreService;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.core.unit.FunctionalUnitService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Opal.FunctionalUnitDto;

public class FunctionalUnitsResourceTest {

  private OpalRuntime opalRuntimeMock;

  private FunctionalUnitService functionalUnitServiceMock;

  private UnitKeyStoreService unitKeyStoreServiceMock;

  private Set<FunctionalUnit> functionalUnits;

  @Before
  public void setUp() {
    opalRuntimeMock = createMock(OpalRuntime.class);
    functionalUnitServiceMock = createMock(FunctionalUnitService.class);

    unitKeyStoreServiceMock = createMock(UnitKeyStoreService.class);

    functionalUnits = new HashSet<FunctionalUnit>();
    functionalUnits.add(new FunctionalUnit("unit1", "key1"));
    functionalUnits.add(new FunctionalUnit("unit2", "key2"));
    functionalUnits.add(new FunctionalUnit("unit3", "key3"));

  }

  @Test
  public void testGetFunctionalUnits() {

    expect(functionalUnitServiceMock.getFunctionalUnits()).andReturn(functionalUnits).once();

    replay(opalRuntimeMock, functionalUnitServiceMock);

    FunctionalUnitsResource functionalUnitsResource = new FunctionalUnitsResource(functionalUnitServiceMock, opalRuntimeMock, unitKeyStoreServiceMock, null, null, "opal-keys.keys");
    List<Opal.FunctionalUnitDto> functionalUnitDtoList = functionalUnitsResource.getFunctionalUnits();
    Assert.assertTrue(functionalUnitDtoList.size() == 3);

    for(FunctionalUnitDto functionalUnitDto : functionalUnitDtoList) {
      Assert.assertTrue(functionalUnitDto.getName().startsWith("unit"));
      Assert.assertTrue(functionalUnitDto.getKeyVariableName().startsWith("key"));
    }

    verify(opalRuntimeMock, functionalUnitServiceMock);

  }
}
