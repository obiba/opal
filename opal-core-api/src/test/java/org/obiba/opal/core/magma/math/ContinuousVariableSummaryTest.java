/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.magma.math;

import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.magma.support.Values;
import org.obiba.magma.type.IntegerType;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
public class ContinuousVariableSummaryTest {

  @BeforeClass
  public static void before() {
    new MagmaEngine();
  }

  @AfterClass
  public static void after() {
    MagmaEngine.get().shutdown();
  }

  @Test
  public void test_compute_integerType() {
    Variable mockVariable = Variable.Builder.newVariable("mock", IntegerType.get(), "mock").build();
    ContinuousVariableSummary summary = computeFromTable(mockVariable, Values.asValues(IntegerType.get(), 1, 2, 3));
    DescriptiveStatistics descriptiveStats = summary.getDescriptiveStats();
    assertThat(descriptiveStats.getMin()).isEqualTo(1.0);
    assertThat(descriptiveStats.getMax()).isEqualTo(3.0);
    assertThat(descriptiveStats.getMean()).isEqualTo(2.0);
    assertThat(descriptiveStats.getN()).isEqualTo(3l);
  }

  @Test
  public void test_compute_integerTypeWithNull() {
    Variable mockVariable = Variable.Builder.newVariable("mock", IntegerType.get(), "mock").build();
    ContinuousVariableSummary summary = computeFromTable(mockVariable,
        Values.asValues(IntegerType.get(), 1, 2, 3, null, null));
    DescriptiveStatistics descriptiveStats = summary.getDescriptiveStats();
    assertThat(descriptiveStats.getMin()).isEqualTo(1.0);
    assertThat(descriptiveStats.getMax()).isEqualTo(3.0);
    assertThat(descriptiveStats.getMean()).isEqualTo(2.0);
    assertThat(descriptiveStats.getN()).isEqualTo(3l);
  }

  @Test
  public void test_compute_integerTypeMissingCategories() {
    Variable mockVariable = Variable.Builder.newVariable("mock", IntegerType.get(), "mock").addCategory("888", "", true)
        .addCategory("999", "", true).build();
    ContinuousVariableSummary summary = computeFromTable(mockVariable,
        Values.asValues(IntegerType.get(), 1, 2, 3, 888, 999));
    DescriptiveStatistics descriptiveStats = summary.getDescriptiveStats();
    assertThat(descriptiveStats.getMin()).isEqualTo(1.0);
    assertThat(descriptiveStats.getMax()).isEqualTo(3.0);
    assertThat(descriptiveStats.getMean()).isEqualTo(2.0);
    assertThat(descriptiveStats.getN()).isEqualTo(3l);
  }

  /*
    @Test
    public void test_compute_withNullValue() {
      Variable mockVariable = Variable.Builder.newVariable("mock", TextType.get(), "mock").addCategories("YES", "NO", "DNK", "PNA").build();
      CategoricalSummaryDto categoricalDto = compute(mockVariable, Values.asValues(TextType.get(), "YES", "NO", null, null));
      assertThat(categoricalDto.getMode()).isEqualTo(CategoricalSummaryResource.NULL_NAME));
    }

    @Test
    public void test_compute_withSequence() {
      Variable mockVariable = Variable.Builder.newVariable("mock", TextType.get(), "mock").addCategories("CAT1", "CAT2").build();
      CategoricalSummaryDto categoricalDto = compute(mockVariable, ImmutableList.of(Values.asSequence(TextType.get(), "CAT1", "CAT2"), Values.asSequence(TextType.get(), "CAT1")));
      assertThat(categoricalDto.getMode()).isEqualTo("CAT1"));
    }

    @Test
    public void test_compute_withSequenceThatContainsNullValue() {
      Variable mockVariable = Variable.Builder.newVariable("mock", TextType.get(), "mock").addCategories("CAT1", "CAT2").build();
      CategoricalSummaryDto categoricalDto = compute(mockVariable, ImmutableList.of(Values.asSequence(TextType.get(), "CAT1", "CAT2"), Values.asSequence(TextType.get(), "CAT1", null)));
      assertThat(categoricalDto.getMode()).isEqualTo("CAT1"));
    }

    @Test
    public void test_compute_withNullSequence() {
      Variable mockVariable = Variable.Builder.newVariable("mock", TextType.get(), "mock").addCategories("CAT1", "CAT2").build();
      CategoricalSummaryDto categoricalDto = compute(mockVariable, ImmutableList.of(TextType.get().nullSequence(), Values.asSequence(TextType.get(), "CAT1")));
      assertThat(categoricalDto.getMode()).isEqualTo("CAT1"));
    }
  */

  private ContinuousVariableSummary computeFromTable(Variable variable, Iterable<Value> values) {
    ValueTable table = mock(ValueTable.class);
    VectorSource vectorSource = mock(VectorSource.class);
    VariableValueSource valueSource = mock(VariableValueSource.class);

    when(vectorSource.getValues(Mockito.<SortedSet<VariableEntity>>any())).thenReturn(values);
    when(valueSource.supportVectorSource()).thenReturn(true);
    when(valueSource.asVectorSource()).thenReturn(vectorSource);
    when(table.getVariableEntities()).thenReturn(new TreeSet<VariableEntity>());
    when(table.getVariableValueSource(variable.getName())).thenReturn(valueSource);

    return new ContinuousVariableSummary.Builder(variable, ContinuousVariableSummary.Distribution.normal)
        .addTable(table, table.getVariableValueSource(variable.getName())).build();
  }

}
