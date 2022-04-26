/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
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

class RResourceCategory implements ResourceProvidersService.Category {

  private final JSONObject categoryObj;

  RResourceCategory(JSONObject categoryObj) {
    this.categoryObj = categoryObj;
  }

  @Override
  public String getName() {
    return categoryObj.optString("name");
  }

  @Override
  public String getTitle() {
    return categoryObj.optString("title");
  }

  @Override
  public String getDescription() {
    return categoryObj.optString("description");
  }
}
