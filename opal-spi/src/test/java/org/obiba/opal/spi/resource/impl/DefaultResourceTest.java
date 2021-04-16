/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.resource.impl;


import org.junit.Test;

import java.net.URISyntaxException;

import static org.fest.assertions.api.Assertions.assertThat;

public class DefaultResourceTest {

  @Test
  public void testToURISegments() throws URISyntaxException {
    DefaultResource res = DefaultResource.newResource("x")
        .scheme("opal+https")
        .host("opal-demo.obiba.org")
        .segments("projects", "foo", "bar.csv")
        .format("csv")
        .build();
    assertThat(res.toURI().toString()).isEqualTo("opal+https://opal-demo.obiba.org/projects/foo/bar.csv");
    assertThat(res.getFormat()).isEqualTo("csv");
  }

  @Test
  public void testToURIPath() throws URISyntaxException {
    DefaultResource res = DefaultResource.newResource("x")
        .scheme("opal+https")
        .host("opal-demo.obiba.org")
        .path("/projects/foo/bar.csv")
        .build();
    assertThat(res.toURI().toString()).isEqualTo("opal+https://opal-demo.obiba.org/projects/foo/bar.csv");
    res = DefaultResource.newResource("x")
        .scheme("file")
        .path("/projects/foo/bar.csv")
        .build();
    assertThat(res.toURI().toString()).isEqualTo("file:///projects/foo/bar.csv");
    res = DefaultResource.newResource("x")
        .scheme("file")
        .path("projects")
        .build();
    assertThat(res.toURI().toString()).isEqualTo("file:///projects");
  }

  @Test
  public void testToURIQuery() throws URISyntaxException {
    DefaultResource res = DefaultResource.newResource("x")
        .scheme("ssh")
        .host("comp1.example.org")
        .port(22)
        .segments("work", "dir")
        .query("exec", "plink,ls")
        .build();
    assertThat(res.toURI().toString()).isEqualTo("ssh://comp1.example.org:22/work/dir?exec=plink,ls");
  }
}
