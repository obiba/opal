/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.rest.client;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.NoHttpResponseException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.ValueTableWriter.ValueSetWriter;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.BinaryType;
import org.obiba.opal.rest.client.magma.RestDatasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.Assert;

/**
 *
 */
public class RestDatasourceTest {

  private static final Logger log = LoggerFactory.getLogger(RestDatasourceTest.class);

  @BeforeClass
  public static void before() {
    new MagmaEngine();
  }

  @AfterClass
  public static void after() {
    MagmaEngine.get().shutdown();
  }

  @Test
  @Ignore
  public void testValueSetWriter() throws URISyntaxException, IOException {
    RestDatasource ds = new RestDatasource("rest", "http://127.0.0.1:8080/ws", "test", "administrator", "password");

    // ensure test table exists
    ValueTableWriter vtw = ds.createWriter("RestDatasourceTest", "Participant");
    VariableWriter vw = vtw.writeVariables();
    Variable var = Variable.Builder.newVariable("VarBin", BinaryType.get(), "Participant").build();
    vw.writeVariable(var);
    vw.close();

    try {
      ValueSetWriter vsw = vtw.writeValueSet(new VariableEntityBean("Participant", "1234"));
      vsw.writeValue(var, BinaryType.get().valueOf(new byte[102400000]));
      vsw.close();
    } catch(NoHttpResponseException e) {
      log.error("{}", e);
      Assert.fail();
    }

  }

}
