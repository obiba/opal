/*
 * Copyright (c) 2024 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web;

import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.core.Response;

public interface BaseResource {

  /**
   * Default OPTIONS implementation, so that {@link org.obiba.opal.web.security.AuthorizationInterceptor} can
   * set the Allow header with appropriate HTTP methods.
   *
   * @return
   */
  @OPTIONS
  default Response getOptions() {
    return Response.ok().build();
  }
}
