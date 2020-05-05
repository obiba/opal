/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.service;

import org.json.JSONObject;
import org.obiba.opal.core.service.ResourceProvidersService;

class RTag implements ResourceProvidersService.Tag {

  private final JSONObject tagObj;

  RTag(JSONObject tagObj) {
    this.tagObj = tagObj;
  }

  @Override
  public String getName() {
    return tagObj.optString("name");
  }

  @Override
  public String getTitle() {
    return tagObj.optString("title");
  }

  @Override
  public String getDescription() {
    return tagObj.optString("description");
  }
}
