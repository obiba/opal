/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.provider;

import static org.hamcrest.CoreMatchers.is;

import org.junit.Assert;
import org.junit.Test;
import org.obiba.opal.web.model.Magma.DatasourceDto;

import com.google.protobuf.JsonFormat;
import com.google.protobuf.JsonFormat.ParseException;

/**
 *
 */
public class JsonFormatTest {

  @Test
  public void test_DtoContainsNonAsciiCharacters() throws ParseException {
    String testValue = "ªºÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖØÙÚÛÜÝÞßàáâäãåæçèéêëìíî";

    String json = JsonFormat.printToString(DatasourceDto.newBuilder().setName(testValue).setType("type").build());
    Assert.assertThat(json, is("{\"name\": \"" + testValue + "\",\"type\": \"type\"}"));

    // Make sure we can read it back
    DatasourceDto.Builder builder = DatasourceDto.newBuilder();
    JsonFormat.merge(json, builder);
    Assert.assertThat(builder.getName(), is(testValue));
  }
}
