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

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.ValueSet;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.StaticValueTable;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;
import org.obiba.opal.core.magma.IdentifiersMappingView.Policy;

import com.google.common.collect.ImmutableSet;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 *
 */
public class IdentifiersMappingViewTest {

  private StaticValueTable opalDataTable;

  private StaticValueTable unitDataTable;

  private StaticValueTable keysTable;

  @Before
  public void setupDataAndKeysTable() {
    MagmaEngine.get();

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
    IdentifiersMappingView fuv = createViewOnOpalDataTable();
    for(VariableEntity entity : fuv.getVariableEntities()) {
      assertThat(entity.getIdentifier().contains("private")).isTrue();
    }
  }

  @Test
  public void test_getVariableEntities_returnsPublicIdentifiers() {
    IdentifiersMappingView fuv = createViewOnUnitDataTable();
    for(VariableEntity entity : fuv.getVariableEntities()) {
      assertThat(entity.getIdentifier().contains("private")).isFalse();
    }
  }

  @Test
  public void test_hasValueSet_returnsTrueForPrivateIdentifier() {
    IdentifiersMappingView fuv = createViewOnOpalDataTable();
    assertThat(fuv.hasValueSet(new VariableEntityBean("Participant", "private-1"))).isTrue();
    assertThat(fuv.hasValueSet(new VariableEntityBean("Participant", "private-2"))).isTrue();
    assertThat(fuv.hasValueSet(new VariableEntityBean("Participant", "private-3"))).isTrue();
    assertThat(fuv.hasValueSet(new VariableEntityBean("Participant", "private-4"))).isTrue();
  }

  @Test
  public void test_hasValueSet_returnsFalseForPrivateIdentifier() {
    // Make unit identifiers private
    IdentifiersMappingView fuv = createViewOnUnitDataTable();
    assertThat(fuv.hasValueSet(new VariableEntityBean("Participant", "private-1"))).isFalse();
    assertThat(fuv.hasValueSet(new VariableEntityBean("Participant", "private-2"))).isFalse();
    assertThat(fuv.hasValueSet(new VariableEntityBean("Participant", "private-3"))).isFalse();
    assertThat(fuv.hasValueSet(new VariableEntityBean("Participant", "private-4"))).isFalse();
  }

  @Test
  public void test_hasValueSet_returnsFalseForPublicIdentifier() {
    IdentifiersMappingView fuv = createViewOnOpalDataTable();
    assertThat(fuv.hasValueSet(new VariableEntityBean("Participant", "1"))).isFalse();
    assertThat(fuv.hasValueSet(new VariableEntityBean("Participant", "2"))).isFalse();
    assertThat(fuv.hasValueSet(new VariableEntityBean("Participant", "3"))).isFalse();
    assertThat(fuv.hasValueSet(new VariableEntityBean("Participant", "4"))).isFalse();
  }

  @Test
  public void test_hasValueSet_returnsTrueForPublicIdentifier() {
    // Make unit identifiers private
    IdentifiersMappingView fuv = createViewOnUnitDataTable();
    assertThat(fuv.hasValueSet(new VariableEntityBean("Participant", "1"))).isTrue();
    assertThat(fuv.hasValueSet(new VariableEntityBean("Participant", "2"))).isTrue();
    assertThat(fuv.hasValueSet(new VariableEntityBean("Participant", "3"))).isTrue();
    assertThat(fuv.hasValueSet(new VariableEntityBean("Participant", "4"))).isTrue();
  }

  @Test
  public void test_getValueSet_returnsValueSetForPrivateIdentifier() {
    IdentifiersMappingView fuv = createViewOnOpalDataTable();
    for(int i = 1; i < 5; i++) {
      ValueSet vs = fuv.getValueSet(new VariableEntityBean("Participant", "private-" + i));
      assertThat(vs.getValueTable()).isEqualTo(fuv);
      assertThat(vs.getVariableEntity().getIdentifier()).isEqualTo("private-" + i);
    }
  }

  @Test
  public void test_getValueSet_returnsValueSetForPublicIdentifier() {
    // Make unit identifiers private
    IdentifiersMappingView fuv = createViewOnUnitDataTable();
    for(int i = 1; i < 5; i++) {
      ValueSet vs = fuv.getValueSet(new VariableEntityBean("Participant", "" + i));
      assertThat(vs.getValueTable()).isEqualTo(fuv);
      assertThat(vs.getVariableEntity().getIdentifier()).isEqualTo("" + i);
    }
  }

  @Test
  public void test_getValueSet_throwsNoSuchValueSetForPublicIdentifier() {
    IdentifiersMappingView fuv = createViewOnOpalDataTable();
    for(int i = 1; i < 5; i++) {
      try {
        fuv.getValueSet(new VariableEntityBean("", "" + i));
        // Must not reach this point
        assertThat(true).isFalse();
      } catch(NoSuchValueSetException e) {
        // should reach this point
      }
    }
  }

  @Test
  public void test_getValueSet_throwsNoSuchValueSetForPrivateIdentifier() {
    // Make unit identifiers private
    IdentifiersMappingView fuv = createViewOnUnitDataTable();
    for(int i = 1; i < 5; i++) {
      try {
        fuv.getValueSet(new VariableEntityBean("", "private-" + i));
        // Must not reach this point
        assertThat(true).isFalse();
      } catch(NoSuchValueSetException e) {
        // should reach this point
      }
    }
  }

  private IdentifiersMappingView createViewOnOpalDataTable() {
    return new IdentifiersMappingView("keys-variable", Policy.UNIT_IDENTIFIERS_ARE_PUBLIC, opalDataTable, keysTable);
  }

  private IdentifiersMappingView createViewOnUnitDataTable() {
    return new IdentifiersMappingView("keys-variable", Policy.UNIT_IDENTIFIERS_ARE_PRIVATE, unitDataTable, keysTable);
  }

}
