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

import org.junit.Test;
import org.obiba.opal.web.model.Opal.AclAction;

import static org.fest.assertions.api.Assertions.assertThat;

public class OpalPermissionsTest {

  @Test
  public void node_is_the_path_below_the_ws_root_whatever_the_context_path() {
    assertThat(node("http://h/ws/project/p")).isEqualTo("/project/p");
    assertThat(node("http://h/opal/ws/project/p")).isEqualTo("/project/p");
    assertThat(node("http://h/opal/ws/datasource/ws/table/t")).isEqualTo("/datasource/ws/table/t");
    assertThat(node("/datasource/ws/table/t")).isEqualTo("/datasource/ws/table/t");
  }

  private static String node(String uri) {
    return new OpalPermissions(URI.create(uri), AclAction.PROJECT_ALL).getNode();
  }
}
