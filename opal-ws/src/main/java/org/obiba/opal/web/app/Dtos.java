/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.app;

import org.obiba.opal.core.runtime.App;
import org.obiba.opal.web.model.Apps;

public class Dtos {

  public static Apps.AppDto asDto(App app) {
    Apps.AppDto.Builder builder = Apps.AppDto.newBuilder()
        .setId(app.getId())
        .setName(app.getName())
        .setType(app.getType())
        .setServer(app.getServer());
    return builder.build();
  }

  public static App fromDto(Apps.AppDto dto) {
    App app = new App();
    if (dto.hasId()) app.setId(dto.getId());
    app.setName(dto.getName());
    app.setType(dto.getType());
    if (dto.hasServer()) app.setServer(dto.getServer());
    return app;
  }
}
