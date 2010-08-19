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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.datasource.nil.NullDatasource;
import org.obiba.magma.support.AbstractDatasource;
import org.obiba.magma.support.StaticValueTable;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.TextType;
import org.obiba.opal.web.model.Magma.DatasourceCompareDto;
import org.obiba.opal.web.model.Magma.TableCompareDto;

/**
 * Unit tests for {@link CompareResource}.
 */
public class CompareResourceTest extends AbstractMagmaResourceTest {
  //
  // Instance Variables
  //

  private Set<Datasource> datasourcesToRemoveAfterTest = new HashSet<Datasource>();

  //
  // Fixture Methods (setUp / tearDown)
  //

  @After
  public void afterTest() {
    for(Datasource ds : datasourcesToRemoveAfterTest) {
      MagmaEngine.get().removeDatasource(ds);
    }
  }

  //
  // Test Methods (for table comparisons)
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
  // Test Methods (for datasource comparisons)
  //

  @Test
  public void testCompare_ReportsNoDifferencesForIdenticalDatasources() {
    // Setup
    ValueTable vtCompared = createTable(new NullDatasource("dummy"), "vt1", "Participant", createVariable("v1", TextType.get(), "Participant"));
    Datasource compared = new StaticDatasource("compared", vtCompared);
    addDatasource(compared);

    ValueTable vtWith = createTable(new NullDatasource("dummy"), "vt1", "Participant", createVariable("v1", TextType.get(), "Participant"));
    Datasource with = new StaticDatasource("with", vtWith);
    addDatasource(with);

    // Exercise
    CompareResource sut = createCompareResource(compared, with);
    Response response = sut.compare("with");

    // Verify
    assertNotNull(response);
    assertTrue(response.getEntity() instanceof DatasourceCompareDto);
    DatasourceCompareDto dto = (DatasourceCompareDto) (response.getEntity());
    assertEquals("compared", dto.getCompared().getName());
    assertEquals("with", dto.getWithDatasource().getName());
    assertEquals(1, dto.getTableComparisonsCount());
    assertEquals("vt1", dto.getTableComparisons(0).getCompared().getName());
    assertEquals("vt1", dto.getTableComparisons(0).getWithTable().getName());
    assertEquals(0, dto.getTableComparisons(0).getNewVariablesCount());
    assertEquals(1, dto.getTableComparisons(0).getExistingVariablesCount());
    assertEquals(0, dto.getTableComparisons(0).getMissingVariablesCount());
    assertEquals(0, dto.getTableComparisons(0).getConflictsCount());
  }

  @Test
  public void testCompare_HandlesCaseWhereTableDoesNotExistInTheSecondDatasource() { // i.e., the "with" datasource
    // Setup
    ValueTable vtCompared = createTable(new NullDatasource("dummy"), "vt1", "Participant", createVariable("v1", TextType.get(), "Participant"));
    Datasource compared = new StaticDatasource("compared", vtCompared);
    addDatasource(compared);

    Datasource with = new StaticDatasource("with");
    addDatasource(with);

    // Exercise
    CompareResource sut = createCompareResource(compared, with);
    Response response = sut.compare("with");

    // Verify
    assertNotNull(response);
    assertTrue(response.getEntity() instanceof DatasourceCompareDto);
    DatasourceCompareDto dto = (DatasourceCompareDto) (response.getEntity());
    assertEquals("compared", dto.getCompared().getName());
    assertEquals("with", dto.getWithDatasource().getName());
    assertEquals(1, dto.getTableComparisonsCount());
    assertEquals("vt1", dto.getTableComparisons(0).getCompared().getName());
    assertFalse(dto.getTableComparisons(0).hasWithTable());
    assertEquals(1, dto.getTableComparisons(0).getNewVariablesCount());
    assertEquals(0, dto.getTableComparisons(0).getExistingVariablesCount());
    assertEquals(0, dto.getTableComparisons(0).getMissingVariablesCount());
    assertEquals(0, dto.getTableComparisons(0).getConflictsCount());
  }

  //
  // Helper Methods
  //

  private CompareResource createCompareResource(final Datasource compared, final Datasource with) {
    return new CompareResource(compared);
  }

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

  private void addDatasource(Datasource ds) {
    MagmaEngine.get().addDatasource(ds);
    datasourcesToRemoveAfterTest.add(ds);
  }

  //
  // Inner Classes
  //

  private static class StaticDatasource extends AbstractDatasource {

    private Map<String, ValueTable> tableMap;

    public StaticDatasource(String name, ValueTable... tablePrototypes) {
      super(name, "static");

      tableMap = new HashMap<String, ValueTable>();
      for(ValueTable vt : tablePrototypes) {
        tableMap.put(vt.getName(), vt);
      }
    }

    @Override
    protected Set<String> getValueTableNames() {
      return tableMap.keySet();
    }

    @Override
    protected ValueTable initialiseValueTable(String tableName) {
      Set<String> entities = Collections.emptySet();
      StaticValueTable valueTable = new StaticValueTable(this, tableName, entities);

      ValueTable tablePrototype = tableMap.get(tableName);
      for(Variable v : tablePrototype.getVariables()) {
        valueTable.addVariables(v.getValueType(), v.getName());
      }

      return valueTable;
    }
  }
}
