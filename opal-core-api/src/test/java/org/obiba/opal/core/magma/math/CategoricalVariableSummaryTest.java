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
import org.obiba.magma.support.NullTimestamps;
import org.obiba.magma.support.Values;
import org.obiba.magma.type.TextType;

import com.google.common.collect.ImmutableList;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class CategoricalVariableSummaryTest {

  @BeforeClass
  public static void before() {
    new MagmaEngine();
  }

  @AfterClass
  public static void after() {
    MagmaEngine.get().shutdown();
  }

  @Test
  public void test_withTextType() {
    Variable variable = Variable.Builder.newVariable("mock", TextType.get(), "mock")
        .addCategories("YES", "NO", "DNK", "PNA").build();
    CategoricalVariableSummary summary = computeFromTable(variable,
        Values.asValues(TextType.get(), "YES", "NO", "YES", "PNA", "DNK"));
    assertThat(summary.getMode(), is("YES"));
  }

  @Test
  public void test_withNullValue() {
    Variable variable = Variable.Builder.newVariable("mock", TextType.get(), "mock")
        .addCategories("YES", "NO", "DNK", "PNA").build();
    CategoricalVariableSummary summary = computeFromTable(variable,
        Values.asValues(TextType.get(), "YES", "NO", null, null));
    assertThat(summary.getMode(), is(CategoricalVariableSummary.NULL_NAME));
  }

  @Test
  public void test_withSequence() {
    Variable variable = Variable.Builder.newVariable("mock", TextType.get(), "mock").addCategories("CAT1", "CAT2")
        .build();
    CategoricalVariableSummary summary = computeFromTable(variable,
        ImmutableList.of(Values.asSequence(TextType.get(), "CAT1", "CAT2"), Values.asSequence(TextType.get(), "CAT1")));
    assertThat(summary.getMode(), is("CAT1"));
  }

  @Test
  public void test_withSequenceThatContainsNullValue() {
    Variable variable = Variable.Builder.newVariable("mock", TextType.get(), "mock").addCategories("CAT1", "CAT2")
        .build();
    CategoricalVariableSummary summary = computeFromTable(variable, ImmutableList
        .of(Values.asSequence(TextType.get(), "CAT1", "CAT2"), Values.asSequence(TextType.get(), "CAT1", null)));
    assertThat(summary.getMode(), is("CAT1"));
  }

  @Test
  public void test_withNullSequence() {
    Variable variable = Variable.Builder.newVariable("mock", TextType.get(), "mock").addCategories("CAT1", "CAT2")
        .build();
    CategoricalVariableSummary summary = computeFromTable(variable,
        ImmutableList.of(TextType.get().nullSequence(), Values.asSequence(TextType.get(), "CAT1")));
    assertThat(summary.getMode(), is("CAT1"));
  }

  private CategoricalVariableSummary computeFromTable(Variable variable, Iterable<Value> values) {

    VectorSource vectorSource = createMock(VectorSource.class);
    expect(vectorSource.getValues(EasyMock.<SortedSet<VariableEntity>>anyObject())).andReturn(values);

    VariableValueSource valueSource = createMock(VariableValueSource.class);
    expect(valueSource.asVectorSource()).andReturn(vectorSource);

    ValueTable table = createMock(ValueTable.class);
    expect(table.getTimestamps()).andReturn(NullTimestamps.get());
    expect(table.getVariableEntities()).andReturn(new TreeSet<VariableEntity>());
    expect(table.getVariableValueSource(variable.getName())).andReturn(valueSource);

    replay(table, vectorSource, valueSource);

    CategoricalVariableSummary summary = new CategoricalVariableSummary.Builder(variable)
        .addTable(table, table.getVariableValueSource(variable.getName())).build();
    assertThat(summary, notNullValue());
    return summary;
  }

}
