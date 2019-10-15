/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.resource;

import org.json.JSONObject;

public interface ResourceFactory {

  /**
   * Resource type unique name.
   *
   * @return
   */
  String getName();

  /**
   * Title of the resource type.
   *
   * @return
   */
  String getTitle();

  /**
   * Markdown can be used to describe the resource type.
   *
   * @return
   */
  String getDescription();

  /**
   * Group to which the resource belongs.
   *
   * @return
   */
  String getGroup();

  /**
   * The form to collect resource type parameters, described by a JSON object.
   *
   * @return
   */
  JSONObject getParametersSchemaForm();

  /**
   * The form to collect resource type credentials, described by a JSON object.
   *
   * @return null if not applicable
   */
  JSONObject getCredentialsSchemaForm();

  /**
   * Make a {@link Resource} object from parameters and credentials that were collected.
   *
   * @param name
   * @param parameters
   * @param credentials can be null if not applicable
   * @return
   */
  Resource createResource(String name, JSONObject parameters, JSONObject credentials);

}
