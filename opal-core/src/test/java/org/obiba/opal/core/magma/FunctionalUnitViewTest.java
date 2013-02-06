/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.magma;

import java.util.Set;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.StaticValueTable;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;
import org.obiba.opal.core.magma.FunctionalUnitView.Policy;
import org.obiba.opal.core.unit.FunctionalUnit;

import com.google.common.collect.ImmutableSet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class FunctionalUnitViewTest {

  StaticValueTable opalDataTable;

  StaticValueTable unitDataTable;

  StaticValueTable keysTable;

  @Before
  public void setupDataAndKeysTable() {
    new MagmaEngine();

    // Create the following table:
    // id,Var1,Var2
    // 1,1,1
    // 2,2,2
    // 3,3,3
    // 4,4,4
    opalDataTable = new StaticValueTable(EasyMock.createMock(Datasource.class), "opal-table",
        ImmutableSet.of("1", "2", "3", "4"));
    opalDataTable.addVariables(IntegerType.get(), "Var1", "Var2");
    for(int i = 1; i < 5; i++) {
      opalDataTable.addValues("" + i, "Var1", i, "Var2", i);
    }

    // Create the following table:
    // id,Var1,Var2
    // private-1,1,1
    // private-1,2,2
    // private-1,3,3
    // private-1,4,4
    unitDataTable = new StaticValueTable(EasyMock.createMock(Datasource.class), "unit-table",
        ImmutableSet.of("private-1", "private-2", "private-3", "private-4"));
    unitDataTable.addVariables(IntegerType.get(), "Var1", "Var2");
    for(int i = 1; i < 5; i++) {
      unitDataTable.addValues("private-" + i, "Var1", i, "Var2", i);
    }

    // Create the following table:
    // id,keys-variable
    // 1,private-1
    // 2,private-2
    // 3,private-3
    // 4,private-4
    keysTable = new StaticValueTable(EasyMock.createMock(Datasource.class), "keys-table",
        ImmutableSet.of("1", "2", "3", "4"));
    keysTable.addVariables(TextType.get(), "keys-variable");
    for(int i = 1; i < 5; i++) {
      keysTable.addValues("" + i, "keys-variable", "private-" + i);
    }

  }

  @After
  public void stopYourEngine() {
    MagmaEngine.get().shutdown();
  }

  @Test
  public void test_getVariableEntities_returnsPrivateIdentifiers() {
    FunctionalUnitView fuv = createViewOnOpalDataTable();
    Set<VariableEntity> entities = fuv.getVariableEntities();
    for(VariableEntity entity : entities) {
      assertThat(entity.getIdentifier().contains("private"), is(true));
    }
  }

  @Test
  public void test_getVariableEntities_returnsPublicIdentifiers() {
    FunctionalUnitView fuv = createViewOnUnitDataTable();
    Set<VariableEntity> entities = fuv.getVariableEntities();
    for(VariableEntity entity : entities) {
      assertThat(entity.getIdentifier().contains("private"), is(false));
    }
  }

  @Test
  public void test_hasValueSet_returnsTrueForPrivateIdentifier() {
    FunctionalUnitView fuv = createViewOnOpalDataTable();
    assertThat(fuv.hasValueSet(new VariableEntityBean("Participant", "private-1")), is(true));
    assertThat(fuv.hasValueSet(new VariableEntityBean("Participant", "private-2")), is(true));
    assertThat(fuv.hasValueSet(new VariableEntityBean("Participant", "private-3")), is(true));
    assertThat(fuv.hasValueSet(new VariableEntityBean("Participant", "private-4")), is(true));
  }

  @Test
  public void test_hasValueSet_returnsFalseForPrivateIdentifier() {
    // Make unit identifiers private
    FunctionalUnitView fuv = createViewOnUnitDataTable();
    assertThat(fuv.hasValueSet(new VariableEntityBean("Participant", "private-1")), is(false));
    assertThat(fuv.hasValueSet(new VariableEntityBean("Participant", "private-2")), is(false));
    assertThat(fuv.hasValueSet(new VariableEntityBean("Participant", "private-3")), is(false));
    assertThat(fuv.hasValueSet(new VariableEntityBean("Participant", "private-4")), is(false));
  }

  @Test
  public void test_hasValueSet_returnsFalseForPublicIdentifier() {
    FunctionalUnitView fuv = createViewOnOpalDataTable();
    assertThat(fuv.hasValueSet(new VariableEntityBean("Participant", "1")), is(false));
    assertThat(fuv.hasValueSet(new VariableEntityBean("Participant", "2")), is(false));
    assertThat(fuv.hasValueSet(new VariableEntityBean("Participant", "3")), is(false));
    assertThat(fuv.hasValueSet(new VariableEntityBean("Participant", "4")), is(false));
  }

  @Test
  public void test_hasValueSet_returnsTrueForPublicIdentifier() {
    // Make unit identifiers private
    FunctionalUnitView fuv = createViewOnUnitDataTable();
    assertThat(fuv.hasValueSet(new VariableEntityBean("Participant", "1")), is(true));
    assertThat(fuv.hasValueSet(new VariableEntityBean("Participant", "2")), is(true));
    assertThat(fuv.hasValueSet(new VariableEntityBean("Participant", "3")), is(true));
    assertThat(fuv.hasValueSet(new VariableEntityBean("Participant", "4")), is(true));
  }

  @Test
  public void test_getValueSet_returnsValueSetForPrivateIdentifier() {
    FunctionalUnitView fuv = createViewOnOpalDataTable();
    for(int i = 1; i < 5; i++) {
      ValueSet vs = fuv.getValueSet(new VariableEntityBean("Participant", "private-" + i));
      assertThat(vs.getValueTable(), is((ValueTable) fuv));
      assertThat(vs.getVariableEntity().getIdentifier(), is("private-" + i));
    }
  }

  @Test
  public void test_getValueSet_returnsValueSetForPublicIdentifier() {
    // Make unit identifiers private
    FunctionalUnitView fuv = createViewOnUnitDataTable();
    for(int i = 1; i < 5; i++) {
      ValueSet vs = fuv.getValueSet(new VariableEntityBean("Participant", "" + i));
      assertThat(vs.getValueTable(), is((ValueTable) fuv));
      assertThat(vs.getVariableEntity().getIdentifier(), is("" + i));
    }
  }

  @Test
  public void test_getValueSet_throwsNoSuchValueSetForPublicIdentifier() {
    FunctionalUnitView fuv = createViewOnOpalDataTable();
    for(int i = 1; i < 5; i++) {
      try {
        fuv.getValueSet(new VariableEntityBean("", "" + i));
        // Must not reach this point
        assertThat(true, is(false));
      } catch(NoSuchValueSetException e) {
        // should reach this point
      }
    }
  }

  @Test
  public void test_getValueSet_throwsNoSuchValueSetForPrivateIdentifier() {
    // Make unit identifiers private
    FunctionalUnitView fuv = createViewOnUnitDataTable();
    for(int i = 1; i < 5; i++) {
      try {
        fuv.getValueSet(new VariableEntityBean("", "private-" + i));
        // Must not reach this point
        assertThat(true, is(false));
      } catch(NoSuchValueSetException e) {
        // should reach this point
      }
    }
  }

  private FunctionalUnitView createViewOnOpalDataTable() {
    return new FunctionalUnitView(new FunctionalUnit("myUnit", "keys-variable"), Policy.UNIT_IDENTIFIERS_ARE_PUBLIC,
        opalDataTable, keysTable);
  }

  private FunctionalUnitView createViewOnUnitDataTable() {
    return new FunctionalUnitView(new FunctionalUnit("myUnit", "keys-variable"), Policy.UNIT_IDENTIFIERS_ARE_PRIVATE,
        unitDataTable, keysTable);
  }

}
