/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.security;

import java.net.URI;

import org.jboss.resteasy.specimpl.ResteasyUriInfo;
import org.junit.Test;
import org.obiba.opal.web.model.Opal.AclAction;

import static org.fest.assertions.api.Assertions.assertThat;

public class OpalPermissionsTest {

  @Test
  public void node_is_the_path_below_the_base_uri_whatever_the_context_path() {
    assertThat(node("/ws", "http://h/ws/project/p")).isEqualTo("/project/p");
    assertThat(node("/opal/ws", "http://h/opal/ws/project/p")).isEqualTo("/project/p");
    assertThat(node("/ws/ws", "http://h/ws/ws/project/p")).isEqualTo("/project/p");
    assertThat(node("/opal/ws", "http://h/opal/ws/datasource/ws/table/t")).isEqualTo("/datasource/ws/table/t");
  }

  @Test
  public void relative_uri_is_the_node() {
    assertThat(new OpalPermissions(URI.create("/datasource/ws/table/t"), AclAction.TABLE_ALL).getNode())
        .isEqualTo("/datasource/ws/table/t");
  }

  private static String node(String contextPath, String uri) {
    return new OpalPermissions(new ResteasyUriInfo(uri, contextPath), URI.create(uri), AclAction.PROJECT_ALL).getNode();
  }
}
