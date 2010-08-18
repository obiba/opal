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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.datasource.nil.NullDatasource;
import org.obiba.magma.support.StaticValueTable;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.TextType;
import org.obiba.opal.web.model.Magma.TableCompareDto;

/**
 * Unit tests for {@link CompareResource}.
 */
public class CompareResourceTest extends AbstractMagmaResourceTest {
  //
  // Test Methods
  //

  @Test
  public void testCompare_ReportsNoDifferencesOrConflictsForIdenticalTables() {
    // Setup
    ValueTable compared = createTable(new NullDatasource("dsCompared"), "compared", "Participant", createVariable("v1", TextType.get(), "Participant"));
    ValueTable with = createTable(new NullDatasource("dsWith"), "with", "Participant", createVariable("v1", TextType.get(), "Participant"));

    // Exercise
    CompareResource sut = createCompareResource(compared, with);
    Response response = sut.compare("dsWith.with");

    // Verify
    assertNotNull(response);
    assertTrue(response.getEntity() instanceof TableCompareDto);
    TableCompareDto dto = (TableCompareDto) (response.getEntity());
    assertEquals(1, dto.getExistingVariablesCount()); // one variable ("v1")
    assertEquals("v1", dto.getExistingVariables(0).getName());
    assertEquals(0, dto.getNewVariablesCount()); // no new variables
    assertEquals(0, dto.getMissingVariablesCount()); // no missing variables
    assertEquals(0, dto.getConflictsCount()); // no conflicts
  }

  @Test
  public void testCompare_ReportsNewVariables() {
    // Setup
    ValueTable compared = createTable(new NullDatasource("dsCompared"), "compared", "Participant", createVariable("v1", TextType.get(), "Participant"), createVariable("v2", TextType.get(), "Participant"));
    ValueTable with = createTable(new NullDatasource("dsWith"), "with", "Participant", createVariable("v1", TextType.get(), "Participant"));

    // Exercise
    CompareResource sut = createCompareResource(compared, with);
    Response response = sut.compare("dsWith.with");

    // Verify
    assertNotNull(response);
    assertTrue(response.getEntity() instanceof TableCompareDto);
    TableCompareDto dto = (TableCompareDto) (response.getEntity());
    assertEquals(1, dto.getExistingVariablesCount()); // one variable ("v1")
    assertEquals("v1", dto.getExistingVariables(0).getName());
    assertEquals(1, dto.getNewVariablesCount()); // one new variable("v2")
    assertEquals("v2", dto.getNewVariables(0).getName());
    assertEquals(0, dto.getMissingVariablesCount()); // no missing variables
    assertEquals(0, dto.getConflictsCount()); // no conflicts
  }

  @Test
  public void testCompare_ReportsMissingVariables() {
    // Setup
    ValueTable compared = createTable(new NullDatasource("dsCompared"), "compared", "Participant", createVariable("v1", TextType.get(), "Participant"));
    ValueTable with = createTable(new NullDatasource("dsWith"), "with", "Participant", createVariable("v1", TextType.get(), "Participant"), createVariable("v2", TextType.get(), "Participant"));

    // Exercise
    CompareResource sut = createCompareResource(compared, with);
    Response response = sut.compare("dsWith.with");

    // Verify
    assertNotNull(response);
    assertTrue(response.getEntity() instanceof TableCompareDto);
    TableCompareDto dto = (TableCompareDto) (response.getEntity());
    assertEquals(1, dto.getExistingVariablesCount()); // one variable ("v1")
    assertEquals("v1", dto.getExistingVariables(0).getName());
    assertEquals(0, dto.getNewVariablesCount()); // no new variables
    assertEquals(1, dto.getMissingVariablesCount()); // one missing variable ("v2")
    assertEquals("v2", dto.getMissingVariables(0).getName());
    assertEquals(0, dto.getConflictsCount()); // no conflicts
  }

  @Test
  public void testCompare_ReportsConflictingEntityTypes() {
    // Setup
    ValueTable compared = createTable(new NullDatasource("dsCompared"), "compared", "Participant", createVariable("v1", TextType.get(), "Participant"));
    ValueTable with = createTable(new NullDatasource("dsWith"), "with", "Instrument", createVariable("v1", TextType.get(), "Instrument"));

    for(Variable v : with.getVariables()) {
      System.out.println("name: " + v.getName() + ", entity type: [" + v.getEntityType() + "]");
    }

    // Exercise
    CompareResource sut = createCompareResource(compared, with);
    Response response = sut.compare("dsWith.with");

    // Verify
    assertNotNull(response);
    assertTrue(response.getEntity() instanceof TableCompareDto);
    TableCompareDto dto = (TableCompareDto) (response.getEntity());
    assertEquals(0, dto.getExistingVariablesCount()); // no existing (non-conflicting) variables
    assertEquals(0, dto.getNewVariablesCount()); // no new variables
    assertEquals(0, dto.getMissingVariablesCount()); // no missing variables
    assertEquals(1, dto.getConflictsCount()); // one conflict (entity type)
    assertEquals("v1", dto.getConflicts(0).getVariable().getName());
    assertEquals("IncompatibleEntityType", dto.getConflicts(0).getCode());
    assertEquals("Participant", dto.getConflicts(0).getArguments(0));
    assertEquals("Instrument", dto.getConflicts(0).getArguments(1));
  }

  @Test
  public void testCompare_ReportsConflictingValueTypes() {
    // Setup
    ValueTable compared = createTable(new NullDatasource("dsCompared"), "compared", "Participant", createVariable("v1", TextType.get(), "Participant"));
    ValueTable with = createTable(new NullDatasource("dsWith"), "with", "Participant", createVariable("v1", BooleanType.get(), "Participant"));

    // Exercise
    CompareResource sut = createCompareResource(compared, with);
    Response response = sut.compare("dsWith.with");

    // Verify
    assertNotNull(response);
    assertTrue(response.getEntity() instanceof TableCompareDto);
    TableCompareDto dto = (TableCompareDto) (response.getEntity());
    assertEquals(0, dto.getExistingVariablesCount()); // no existing (non-conflicting) variables
    assertEquals(0, dto.getNewVariablesCount()); // no new variables
    assertEquals(0, dto.getMissingVariablesCount()); // no missing variables
    assertEquals(1, dto.getConflictsCount()); // one conflict (value type)
    assertEquals("v1", dto.getConflicts(0).getVariable().getName());
    assertEquals("IncompatibleValueType", dto.getConflicts(0).getCode());
    assertEquals(2, dto.getConflicts(0).getArgumentsCount());
    assertEquals("text", dto.getConflicts(0).getArguments(0));
    assertEquals("boolean", dto.getConflicts(0).getArguments(1));
  }

  //
  // Helper Methods
  //

  private CompareResource createCompareResource(final ValueTable compared, final ValueTable with) {
    return new CompareResource(compared) {

      ValueTable getValueTable(String fqTableName) {
        return with;
      }
    };
  }

  private ValueTable createTable(Datasource datasource, String name, String entityType, Variable... variables) {
    Set<String> entities = Collections.emptySet();
    StaticValueTable valueTable = new StaticValueTable(datasource, name, entities, entityType);

    for(Variable v : variables) {
      valueTable.addVariables(v.getValueType(), v.getName());
    }

    return valueTable;
  }

  private Variable createVariable(String name, ValueType type, String entityType) {
    return Variable.Builder.newVariable(name, type, entityType).build();
  }
}
