/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.math;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

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
import org.obiba.magma.VectorSource;
import org.obiba.magma.support.Values;
import org.obiba.magma.type.TextType;
import org.obiba.opal.web.model.Math.CategoricalSummaryDto;
import org.obiba.opal.web.model.Math.SummaryStatisticsDto;

import com.google.common.collect.ImmutableList;

/**
 *
 */
public class CategoricalSummaryStatisticsResourceTest {

  @BeforeClass
  public static void before() {
    new MagmaEngine();
  }

  @AfterClass
  public static void after() {
    MagmaEngine.get().shutdown();
  }

  @Test
  public void test_compute_withTextType() {
    Variable mockVariable = Variable.Builder.newVariable("mock", TextType.get(), "mock").addCategories("YES", "NO", "DNK", "PNA").build();
    CategoricalSummaryDto categoricalDto = compute(mockVariable, Values.asValues(TextType.get(), "YES", "NO", "YES", "PNA", "DNK"));
    assertThat(categoricalDto.getMode(), is("YES"));
  }

  @Test
  public void test_compute_withNullValue() {
    Variable mockVariable = Variable.Builder.newVariable("mock", TextType.get(), "mock").addCategories("YES", "NO", "DNK", "PNA").build();
    CategoricalSummaryDto categoricalDto = compute(mockVariable, Values.asValues(TextType.get(), "YES", "NO", null, null));
    assertThat(categoricalDto.getMode(), is(CategoricalSummaryStatisticsResource.NULL_NAME));
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

  private CategoricalSummaryDto compute(Variable variable, Iterable<Value> values) {
    ValueTable mockTable = createMock(ValueTable.class);
    VectorSource mockSource = createMock(VectorSource.class);

    expect(mockTable.getVariableEntities()).andReturn(new TreeSet<VariableEntity>());
    expect(mockSource.getValues((SortedSet<VariableEntity>) EasyMock.anyObject())).andReturn(values);

    replay(mockTable, mockSource);
    CategoricalSummaryStatisticsResource resource = new CategoricalSummaryStatisticsResource(mockTable, variable, mockSource);
    SummaryStatisticsDto dto = resource.compute();
    verify(mockTable, mockSource);

    assertThat(dto, notNullValue());
    assertThat(dto.hasExtension(CategoricalSummaryDto.categorical), is(true));
    CategoricalSummaryDto categoricalDto = dto.getExtension(CategoricalSummaryDto.categorical);
    return categoricalDto;
  }
}
