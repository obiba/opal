/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.ws.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.obiba.opal.web.model.Magma.DatasourceDto;

/**
 *
 */
public class JsonIoUtilTest {

  @Test
  public void test_printCollectionWithEmptyCollection() throws IOException {
    StringBuilder output = new StringBuilder();
    JsonIoUtil.printCollection(createDtos(), output);
    assertThat(output.toString(), is("[]"));
  }

  @Test
  public void test_printCollectionWithSingletonCollection() throws IOException {
    StringBuilder output = new StringBuilder();
    JsonIoUtil.printCollection(createDtos("singleton"), output);
    assertThat(output.toString(), is(createJs("singleton")));
  }

  @Test
  public void test_printCollectionWithTwoElements() throws IOException {
    StringBuilder output = new StringBuilder();
    JsonIoUtil.printCollection(createDtos("first", "second"), output);
    assertThat(output.toString(), is(createJs("first", "second")));
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_printCollection_cannotPassNullCollection() throws IOException {
    JsonIoUtil.printCollection(null, new StringBuilder());
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_printCollection_cannotPassNullApendable() throws IOException {
    JsonIoUtil.printCollection(createDtos(), null);
  }

  @Test
  public void test_mergeCollectionWithEmptyArray() throws IOException {
    Collection<DatasourceDto> msgs = JsonIoUtil.mergeCollection(new StringReader("[]"), DatasourceDto.newBuilder());
    assertThat(msgs, notNullValue());
    assertThat(msgs.size(), is(0));
  }

  @Test
  public void test_mergeCollectionSingleElement() throws IOException {
    Collection<DatasourceDto> msgs = JsonIoUtil.mergeCollection(new StringReader(createJs("singleton")), DatasourceDto.newBuilder());
    assertThat(msgs, notNullValue());
    assertThat(msgs, is(createDtos("singleton")));
  }

  @Test
  public void test_mergeCollectionMultipleElements() throws IOException {
    Collection<DatasourceDto> msgs = JsonIoUtil.mergeCollection(new StringReader(createJs("first", "second")), DatasourceDto.newBuilder());
    assertThat(msgs, notNullValue());
    assertThat(msgs, is(createDtos("first", "second")));
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_mergeCollection_cannotPassNullReader() throws IOException {
    JsonIoUtil.mergeCollection(null, DatasourceDto.newBuilder());
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_mergeCollection_cannotPassNullBuilder() throws IOException {
    JsonIoUtil.mergeCollection(new StringReader(""), null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_mergeCollection_cannotPassNullExtensionRegistry() throws IOException {
    JsonIoUtil.mergeCollection(new StringReader(""), null, DatasourceDto.newBuilder());
  }

  private Collection<DatasourceDto> createDtos(String... names) {
    ArrayList<DatasourceDto> dtos = new ArrayList<DatasourceDto>(names.length);
    for(String name : names) {
      dtos.add(DatasourceDto.newBuilder().setName(name).setType("type").setLink("http://localhost/" + name).build());
    }
    return dtos;
  }

  /**
   * Creates the expected JSON string for a list of names. Each element in the {@code names} array will produce this:
   * 
   * <pre>
   * {"name": "names[i]","link": "http://localhost/names[i]"}
   * </pre>
   * 
   * They are all wrapped in a JSON array:
   * 
   * <pre>
   * [{...},{...},...]
   * </pre>
   * 
   * An empty array produces an empty JSON array:
   * 
   * <pre>
   * []
   * </pre>
   * 
   * @param names
   * @return
   */
  private static String createJs(String... names) {
    StringBuilder sb = new StringBuilder();
    sb.append('[');
    for(int i = 0; i < names.length; i++) {
      if(i > 0) sb.append(',');
      sb.append("{\"name\": \"").append(names[i]).append("\",\"link\": \"").append("http://localhost/").append(names[i]).append("\",\"type\": \"type\"}");
    }
    sb.append(']');
    return sb.toString();
  }
}
