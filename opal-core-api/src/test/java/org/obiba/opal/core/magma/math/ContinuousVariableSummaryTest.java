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

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.easymock.EasyMock;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.magma.support.Values;
import org.obiba.magma.type.IntegerType;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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
    assertThat(descriptiveStats.getMin(), is(1.0));
    assertThat(descriptiveStats.getMax(), is(3.0));
    assertThat(descriptiveStats.getMean(), is(2.0));
    assertThat(descriptiveStats.getN(), is(3l));
  }

  @Test
  public void test_compute_integerTypeWithNull() {
    Variable mockVariable = Variable.Builder.newVariable("mock", IntegerType.get(), "mock").build();
    ContinuousVariableSummary summary = computeFromTable(mockVariable,
        Values.asValues(IntegerType.get(), 1, 2, 3, null, null));
    DescriptiveStatistics descriptiveStats = summary.getDescriptiveStats();
    assertThat(descriptiveStats.getMin(), is(1.0));
    assertThat(descriptiveStats.getMax(), is(3.0));
    assertThat(descriptiveStats.getMean(), is(2.0));
    assertThat(descriptiveStats.getN(), is(3l));
  }

  @Test
  public void test_compute_integerTypeMissingCategories() {
    Variable mockVariable = Variable.Builder.newVariable("mock", IntegerType.get(), "mock").addCategory("888", "", true)
        .addCategory("999", "", true).build();
    ContinuousVariableSummary summary = computeFromTable(mockVariable,
        Values.asValues(IntegerType.get(), 1, 2, 3, 888, 999));
    DescriptiveStatistics descriptiveStats = summary.getDescriptiveStats();
    assertThat(descriptiveStats.getMin(), is(1.0));
    assertThat(descriptiveStats.getMax(), is(3.0));
    assertThat(descriptiveStats.getMean(), is(2.0));
    assertThat(descriptiveStats.getN(), is(3l));
  }

  /*
    @Test
    public void test_compute_withNullValue() {
      Variable mockVariable = Variable.Builder.newVariable("mock", TextType.get(), "mock").addCategories("YES", "NO", "DNK", "PNA").build();
      CategoricalSummaryDto categoricalDto = compute(mockVariable, Values.asValues(TextType.get(), "YES", "NO", null, null));
      assertThat(categoricalDto.getMode(), is(CategoricalSummaryResource.NULL_NAME));
    }

    @Test
    public void test_compute_withSequence() {
      Variable mockVariable = Variable.Builder.newVariable("mock", TextType.get(), "mock").addCategories("CAT1", "CAT2").build();
      CategoricalSummaryDto categoricalDto = compute(mockVariable, ImmutableList.of(Values.asSequence(TextType.get(), "CAT1", "CAT2"), Values.asSequence(TextType.get(), "CAT1")));
      assertThat(categoricalDto.getMode(), is("CAT1"));
    }

    @Test
    public void test_compute_withSequenceThatContainsNullValue() {
      Variable mockVariable = Variable.Builder.newVariable("mock", TextType.get(), "mock").addCategories("CAT1", "CAT2").build();
      CategoricalSummaryDto categoricalDto = compute(mockVariable, ImmutableList.of(Values.asSequence(TextType.get(), "CAT1", "CAT2"), Values.asSequence(TextType.get(), "CAT1", null)));
      assertThat(categoricalDto.getMode(), is("CAT1"));
    }

    @Test
    public void test_compute_withNullSequence() {
      Variable mockVariable = Variable.Builder.newVariable("mock", TextType.get(), "mock").addCategories("CAT1", "CAT2").build();
      CategoricalSummaryDto categoricalDto = compute(mockVariable, ImmutableList.of(TextType.get().nullSequence(), Values.asSequence(TextType.get(), "CAT1")));
      assertThat(categoricalDto.getMode(), is("CAT1"));
    }
  */

  private ContinuousVariableSummary computeFromTable(Variable variable, Iterable<Value> values) {
    ValueTable table = createMock(ValueTable.class);
    VectorSource vectorSource = createMock(VectorSource.class);
    VariableValueSource valueSource = createMock(VariableValueSource.class);

    expect(vectorSource.getValues(EasyMock.<SortedSet<VariableEntity>>anyObject())).andReturn(values);
    expect(valueSource.asVectorSource()).andReturn(vectorSource);
    expect(table.getVariableEntities()).andReturn(new TreeSet<VariableEntity>());
    expect(table.getVariableValueSource(variable.getName())).andReturn(valueSource);

    replay(table, vectorSource, valueSource);

    ContinuousVariableSummary summary = new ContinuousVariableSummary.Builder(variable,
        ContinuousVariableSummary.Distribution.normal).addTable(table).build();

    verify(table, vectorSource, valueSource);

    return summary;
  }

}
