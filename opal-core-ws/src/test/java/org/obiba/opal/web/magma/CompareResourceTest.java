/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.ws.rs.core.Response;

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

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Unit tests for {@link CompareResourceImpl}.
 */
public class CompareResourceTest extends AbstractMagmaResourceTest {
  //
  // Instance Variables
  //

  private final Collection<Datasource> datasourcesToRemoveAfterTest = new HashSet<>();

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
    ValueTable compared = createTable(new NullDatasource("dsCompared"), "compared", "Participant",
        createVariable("v1", TextType.get(), "Participant"));
    ValueTable with = createTable(new NullDatasource("dsWith"), "with", "Participant",
        createVariable("v1", TextType.get(), "Participant"));

    // Exercise
    CompareResource sut = createCompareResource(compared, with);
    Response response = sut.compare("dsWith.with", false);

    // Verify
    assertThat(response).isNotNull();
    assertThat(response.getEntity()).isInstanceOf(TableCompareDto.class);
    TableCompareDto dto = (TableCompareDto) response.getEntity();
    assertThat(1).isEqualTo(dto.getUnmodifiedVariablesCount()); // one variable ("v1")
    assertThat(0).isEqualTo(dto.getModifiedVariablesCount()); // no variable
    assertThat("v1").isEqualTo(dto.getUnmodifiedVariables(0).getName());
    assertThat(0).isEqualTo(dto.getNewVariablesCount()); // no new variables
    assertThat(0).isEqualTo(dto.getMissingVariablesCount()); // no missing variables
    assertThat(0).isEqualTo(dto.getConflictsCount()); // no conflicts
  }

  @Test
  public void testCompare_ReportsCategoriesModifiedForIdenticalTables() {
    // Setup
    ValueTable compared = createTable(new NullDatasource("dsCompared"), "compared", "Participant",
        createVariable("v1", TextType.get(), "Participant"));
    ValueTable with = createTable(new NullDatasource("dsWith"), "with", "Participant",
        createVariable("v1", TextType.get(), "Participant", "A", "B", "C"));

    // Exercise
    CompareResource sut = createCompareResource(compared, with);
    Response response = sut.compare("dsWith.with", false);

    // Verify
    assertThat(response).isNotNull();
    assertThat(response.getEntity()).isInstanceOf(TableCompareDto.class);
    TableCompareDto dto = (TableCompareDto) response.getEntity();
    assertThat(0).isEqualTo(dto.getUnmodifiedVariablesCount()); // one variable ("v1")
    assertThat(1).isEqualTo(dto.getModifiedVariablesCount()); // one variable ("v1")
    assertThat("v1").isEqualTo(dto.getModifiedVariables(0).getName());
    assertThat(0).isEqualTo(dto.getNewVariablesCount()); // no new variables
    assertThat(0).isEqualTo(dto.getMissingVariablesCount()); // no missing variables
    assertThat(0).isEqualTo(dto.getConflictsCount()); // no conflicts
  }

  @Test
  public void testCompare_ReportsAttributesModifiedForIdenticalTables() {
    // Setup
    ValueTable compared = createTable(new NullDatasource("dsCompared"), "compared", "Participant",
        createVariableBuilder("v1", TextType.get(), "Participant").addAttribute("patate", "pourrie").build());
    ValueTable with = createTable(new NullDatasource("dsWith"), "with", "Participant",
        createVariableBuilder("v1", TextType.get(), "Participant").addAttribute("patate", "pwel").build());

    // Exercise
    CompareResource sut = createCompareResource(compared, with);
    Response response = sut.compare("dsWith.with", false);

    // Verify
    assertThat(response).isNotNull();
    assertThat(response.getEntity()).isInstanceOf(TableCompareDto.class);
    TableCompareDto dto = (TableCompareDto) response.getEntity();
    assertThat(0).isEqualTo(dto.getUnmodifiedVariablesCount()); // one variable ("v1")
    assertThat(1).isEqualTo(dto.getModifiedVariablesCount()); // one variable ("v1")
    assertThat("v1").isEqualTo(dto.getModifiedVariables(0).getName());
    assertThat(0).isEqualTo(dto.getNewVariablesCount()); // no new variables
    assertThat(0).isEqualTo(dto.getMissingVariablesCount()); // no missing variables
    assertThat(0).isEqualTo(dto.getConflictsCount()); // no conflicts
  }

  @Test
  public void testCompare_ReportsNewVariables() {
    // Setup
    ValueTable compared = createTable(new NullDatasource("dsCompared"), "compared", "Participant",
        createVariable("v1", TextType.get(), "Participant"), createVariable("v2", TextType.get(), "Participant"));
    ValueTable with = createTable(new NullDatasource("dsWith"), "with", "Participant",
        createVariable("v1", TextType.get(), "Participant"));

    // Exercise
    CompareResource sut = createCompareResource(compared, with);
    Response response = sut.compare("dsWith.with", false);

    // Verify
    assertThat(response).isNotNull();
    assertThat(response.getEntity()).isInstanceOf(TableCompareDto.class);
    TableCompareDto dto = (TableCompareDto) response.getEntity();
    assertThat(1).isEqualTo(dto.getUnmodifiedVariablesCount()); // one variable ("v1")
    assertThat(0).isEqualTo(dto.getModifiedVariablesCount()); // no variable
    assertThat("v1").isEqualTo(dto.getUnmodifiedVariables(0).getName());
    assertThat(1).isEqualTo(dto.getNewVariablesCount()); // one new variable("v2")
    assertThat("v2").isEqualTo(dto.getNewVariables(0).getName());
    assertThat(0).isEqualTo(dto.getMissingVariablesCount()); // no missing variables
    assertThat(0).isEqualTo(dto.getConflictsCount()); // no conflicts
  }

  @Test
  public void testCompare_ReportsMissingVariables() {
    // Setup
    ValueTable compared = createTable(new NullDatasource("dsCompared"), "compared", "Participant",
        createVariable("v1", TextType.get(), "Participant"));
    ValueTable with = createTable(new NullDatasource("dsWith"), "with", "Participant",
        createVariable("v1", TextType.get(), "Participant"), createVariable("v2", TextType.get(), "Participant"));

    // Exercise
    CompareResource sut = createCompareResource(compared, with);
    Response response = sut.compare("dsWith.with", false);

    // Verify
    assertThat(response).isNotNull();
    assertThat(response.getEntity()).isInstanceOf(TableCompareDto.class);
    TableCompareDto dto = (TableCompareDto) response.getEntity();
    assertThat(1).isEqualTo(dto.getUnmodifiedVariablesCount()); // one variable ("v1")
    assertThat(0).isEqualTo(dto.getModifiedVariablesCount()); // no variable
    assertThat("v1").isEqualTo(dto.getUnmodifiedVariables(0).getName());
    assertThat(0).isEqualTo(dto.getNewVariablesCount()); // no new variables
    assertThat(1).isEqualTo(dto.getMissingVariablesCount()); // one missing variable ("v2")
    assertThat("v2").isEqualTo(dto.getMissingVariables(0).getName());
    assertThat(0).isEqualTo(dto.getConflictsCount()); // no conflicts
  }

  @Test
  public void testCompare_ReportsConflictingEntityTypes() {
    // Setup
    ValueTable compared = createTable(new NullDatasource("dsCompared"), "compared", "Participant",
        createVariable("v1", TextType.get(), "Participant"));
    ValueTable with = createTable(new NullDatasource("dsWith"), "with", "Instrument",
        createVariable("v1", TextType.get(), "Instrument"));

    // Exercise
    CompareResource sut = createCompareResource(compared, with);
    Response response = sut.compare("dsWith.with", false);

    // Verify
    assertThat(response).isNotNull();
    assertThat(response.getEntity()).isInstanceOf(TableCompareDto.class);
    TableCompareDto dto = (TableCompareDto) response.getEntity();
    assertThat(0).isEqualTo(dto.getUnmodifiedVariablesCount()); // no existing (non-conflicting) variables
    assertThat(0).isEqualTo(dto.getNewVariablesCount()); // no new variables
    assertThat(0).isEqualTo(dto.getMissingVariablesCount()); // no missing variables
    assertThat(1).isEqualTo(dto.getConflictsCount()); // one conflict (entity type)
    assertThat("v1").isEqualTo(dto.getConflicts(0).getVariable().getName());
    assertThat("IncompatibleEntityType").isEqualTo(dto.getConflicts(0).getCode());
    assertThat("Participant").isEqualTo(dto.getConflicts(0).getArguments(0));
    assertThat("Instrument").isEqualTo(dto.getConflicts(0).getArguments(1));
  }

  @Test
  public void testCompare_ReportsConflictingValueTypes() {
    // Setup
    ValueTable compared = createTable(new NullDatasource("dsCompared"), "compared", "Participant",
        createVariable("v1", TextType.get(), "Participant"));
    ValueTable with = createTable(new NullDatasource("dsWith"), "with", "Participant",
        createVariable("v1", BooleanType.get(), "Participant"));

    // Exercise
    CompareResource sut = createCompareResource(compared, with);
    Response response = sut.compare("dsWith.with", false);

    // Verify
    assertThat(response).isNotNull();
    assertThat(response.getEntity()).isInstanceOf(TableCompareDto.class);
    TableCompareDto dto = (TableCompareDto) response.getEntity();
    assertThat(0).isEqualTo(dto.getUnmodifiedVariablesCount()); // no existing (non-conflicting) variables
    assertThat(0).isEqualTo(dto.getNewVariablesCount()); // no new variables
    assertThat(0).isEqualTo(dto.getMissingVariablesCount()); // no missing variables
    assertThat(1).isEqualTo(dto.getConflictsCount()); // one conflict (value type)
    assertThat("v1").isEqualTo(dto.getConflicts(0).getVariable().getName());
    assertThat("IncompatibleValueType").isEqualTo(dto.getConflicts(0).getCode());
    assertThat(2).isEqualTo(dto.getConflicts(0).getArgumentsCount());
    assertThat("text").isEqualTo(dto.getConflicts(0).getArguments(0));
    assertThat("boolean").isEqualTo(dto.getConflicts(0).getArguments(1));
  }

  //
  // Test Methods (for datasource comparisons)
  //

  @SuppressWarnings({ "OverlyLongMethod", "PMD.NcssMethodCount" })
  @Test
  public void testCompare_ReportsNoDifferencesForIdenticalDatasources() {
    // Setup
    ValueTable vtCompared = createTable(new NullDatasource("dummy"), "vt1", "Participant",
        createVariable("v1", TextType.get(), "Participant"));
    Datasource compared = new StaticDatasource("compared", vtCompared);
    addDatasource(compared);

    ValueTable vtWith = createTable(new NullDatasource("dummy"), "vt1", "Participant",
        createVariable("v1", TextType.get(), "Participant"));
    Datasource with = new StaticDatasource("with", vtWith);
    addDatasource(with);

    // Exercise
    CompareResource sut = createCompareResource(compared);
    Response response = sut.compare("with", false);

    // Verify
    assertThat(response).isNotNull();
    assertThat(response.getEntity() instanceof DatasourceCompareDto).isTrue();
    DatasourceCompareDto dto = (DatasourceCompareDto) response.getEntity();
    assertThat("compared").isEqualTo(dto.getCompared().getName());
    assertThat("with").isEqualTo(dto.getWithDatasource().getName());
    assertThat(1).isEqualTo(dto.getTableComparisonsCount());
    assertThat("vt1").isEqualTo(dto.getTableComparisons(0).getCompared().getName());
    assertThat("vt1").isEqualTo(dto.getTableComparisons(0).getWithTable().getName());
    assertThat(0).isEqualTo(dto.getTableComparisons(0).getNewVariablesCount());
    assertThat(1).isEqualTo(dto.getTableComparisons(0).getUnmodifiedVariablesCount());
    assertThat(0).isEqualTo(dto.getTableComparisons(0).getModifiedVariablesCount());
    assertThat(0).isEqualTo(dto.getTableComparisons(0).getMissingVariablesCount());
    assertThat(0).isEqualTo(dto.getTableComparisons(0).getConflictsCount());
  }

  @Test
  public void testCompare_HandlesCaseWhereTableDoesNotExistInTheSecondDatasource() { // i.e., the "with" datasource
    // Setup
    ValueTable vtCompared = createTable(new NullDatasource("dummy"), "vt1", "Participant",
        createVariable("v1", TextType.get(), "Participant"));
    Datasource compared = new StaticDatasource("compared", vtCompared);
    addDatasource(compared);

    Datasource with = new StaticDatasource("with");
    addDatasource(with);

    // Exercise
    CompareResource sut = createCompareResource(compared);
    Response response = sut.compare("with", false);

    // Verify
    assertThat(response).isNotNull();
    assertThat(response.getEntity()).isInstanceOf(DatasourceCompareDto.class);
    DatasourceCompareDto dto = (DatasourceCompareDto) response.getEntity();
    assertThat("compared").isEqualTo(dto.getCompared().getName());
    assertThat("with").isEqualTo(dto.getWithDatasource().getName());
    assertThat(1).isEqualTo(dto.getTableComparisonsCount());
    assertThat("vt1").isEqualTo(dto.getTableComparisons(0).getCompared().getName());
    assertThat(dto.getTableComparisons(0).hasWithTable()).isFalse();
    assertThat(1).isEqualTo(dto.getTableComparisons(0).getNewVariablesCount());
    assertThat(0).isEqualTo(dto.getTableComparisons(0).getUnmodifiedVariablesCount());
    assertThat(0).isEqualTo(dto.getTableComparisons(0).getModifiedVariablesCount());
    assertThat(0).isEqualTo(dto.getTableComparisons(0).getMissingVariablesCount());
    assertThat(0).isEqualTo(dto.getTableComparisons(0).getConflictsCount());
  }

  //
  // Helper Methods
  //

  private CompareResource createCompareResource(Datasource compared) {
    CompareResource resource = new CompareResourceImpl();
    resource.setComparedDatasource(compared);
    return resource;
  }

  private CompareResource createCompareResource(ValueTable compared, final ValueTable with) {
    CompareResource resource = new CompareResourceImpl() {

      @Override
      ValueTable getValueTable(String fqTableName) {
        return with;
      }
    };
    resource.setComparedTable(compared);
    return resource;
  }

  private ValueTable createTable(Datasource datasource, String name, String entityType, Variable... variables) {
    Set<String> entities = Collections.emptySet();
    StaticValueTable valueTable = new StaticValueTable(datasource, name, entities, entityType);

    for(Variable v : variables) {
      valueTable.addVariable(v);
    }

    return valueTable;
  }

  private Variable createVariable(String name, ValueType type, String entityType, String... categories) {
    Variable.Builder builder = createVariableBuilder(name, type, entityType);
    if(categories != null) {
      builder.addCategories(categories);
    }
    return builder.build();
  }

  private Variable.Builder createVariableBuilder(String name, ValueType type, String entityType) {
    return Variable.Builder.newVariable(name, type, entityType);
  }

  private void addDatasource(Datasource ds) {
    MagmaEngine.get().addDatasource(ds);
    datasourcesToRemoveAfterTest.add(ds);
  }

  //
  // Inner Classes
  //

  private static class StaticDatasource extends AbstractDatasource {

    private final Map<String, ValueTable> tableMap;

    private StaticDatasource(String name, ValueTable... tablePrototypes) {
      super(name, "static");

      tableMap = new HashMap<>();
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
