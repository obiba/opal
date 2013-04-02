/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.math;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.obiba.magma.MagmaEngine;

/**
 *
 */
public class ContinuousSummaryResourceTest {

  @BeforeClass
  public static void before() {
    new MagmaEngine();
  }

  @AfterClass
  public static void after() {
    MagmaEngine.get().shutdown();
  }

//  @Test
//  public void test_compute_integerType() {
//    Variable mockVariable = Variable.Builder.newVariable("mock", IntegerType.get(), "mock").build();
//    ContinuousSummaryDto continuousDto = compute(mockVariable, Values.asValues(IntegerType.get(), 1, 2, 3));
//    assertThat(continuousDto.getSummary().getMin(), is(1.0));
//    assertThat(continuousDto.getSummary().getMax(), is(3.0));
//    assertThat(continuousDto.getSummary().getMean(), is(2.0));
//    assertThat(continuousDto.getSummary().getN(), is(3l));
//  }
//
//  @Test
//  public void test_compute_integerTypeWithNull() {
//    Variable mockVariable = Variable.Builder.newVariable("mock", IntegerType.get(), "mock").build();
//    ContinuousSummaryDto continuousDto = compute(mockVariable, Values.asValues(IntegerType.get(), 1, 2, 3, null, null));
//    assertThat(continuousDto.getSummary().getMin(), is(1.0));
//    assertThat(continuousDto.getSummary().getMax(), is(3.0));
//    assertThat(continuousDto.getSummary().getMean(), is(2.0));
//    assertThat(continuousDto.getSummary().getN(), is(3l));
//  }
//
//  @Test
//  public void test_compute_integerTypeMissingCategories() {
//    Variable mockVariable = Variable.Builder.newVariable("mock", IntegerType.get(), "mock").addCategory("888", "", true)
//        .addCategory("999", "", true).build();
//    ContinuousSummaryDto continuousDto = compute(mockVariable, Values.asValues(IntegerType.get(), 1, 2, 3, 888, 999));
//    assertThat(continuousDto.getSummary().getMin(), is(1.0));
//    assertThat(continuousDto.getSummary().getMax(), is(3.0));
//    assertThat(continuousDto.getSummary().getMean(), is(2.0));
//    assertThat(continuousDto.getSummary().getN(), is(3l));
//  }

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
//  @SuppressWarnings("unchecked")
//  private ContinuousSummaryDto compute(Variable variable, Iterable<Value> values) {
//    ValueTable mockTable = createMock(ValueTable.class);
//    VectorSource mockSource = createMock(VectorSource.class);
//
//    expect(mockTable.getTimestamps()).andReturn(NullTimestamps.get());
//    expect(mockTable.getVariableEntities()).andReturn(new TreeSet<VariableEntity>());
//    expect(mockSource.getValues((SortedSet<VariableEntity>) EasyMock.anyObject())).andReturn(values);
//
//    replay(mockTable, mockSource);
//    ContinuousSummaryResource resource = new ContinuousSummaryResource(mockTable, variable, mockSource);
//    SummaryStatisticsDto dto = (SummaryStatisticsDto) resource
//        .compute(Distribution.normal, ImmutableList.<Double>of(), 10).getEntity();
//    verify(mockTable, mockSource);
//
//    assertThat(dto, notNullValue());
//    assertThat(dto.hasExtension(ContinuousSummaryDto.continuous), is(true));
//    return dto.getExtension(ContinuousSummaryDto.continuous);
//  }
}
