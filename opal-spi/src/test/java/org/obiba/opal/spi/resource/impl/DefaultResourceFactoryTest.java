/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.resource.impl;


import org.json.JSONObject;
import org.junit.Test;
import org.obiba.opal.spi.resource.Resource;
import org.obiba.opal.spi.resource.ResourceFactory;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import static org.fest.assertions.api.Assertions.assertThat;

public class DefaultResourceFactoryTest {

  @Test
  public void testResourceCreationFromJs() throws Exception {
    ResourceFactory resourceFactory = new DefaultResourceFactory(new File("/tmp")) {
      @Override
      protected Reader getToResourceScriptReader() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("resource.js");
        return new InputStreamReader(is, StandardCharsets.UTF_8);
      }
    };
    JSONObject params = new JSONObject("{ path: '/work/dir', format: 'csv' }");
    JSONObject credentials = new JSONObject("{ username: 'user1', password: '1234' }");
    Resource resource = resourceFactory.createResource("toto", params, credentials);
    assertThat(resource.getName()).isEqualTo("toto");
    assertThat(resource.toURI().toString()).isEqualTo("file:///work/dir");
    assertThat(resource.getFormat()).isEqualTo("csv");
    assertThat(resource.getCredentials().getIdentity()).isEqualTo("user1");
    assertThat(resource.getCredentials().getSecret()).isEqualTo("1234");
  }
}
