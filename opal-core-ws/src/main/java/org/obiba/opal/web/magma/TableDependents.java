/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma;

import jakarta.ws.rs.core.Response;
import org.apache.shiro.SecurityUtils;
import org.obiba.opal.core.cfg.OpalViewManager.DependentView;

import java.util.List;

/**
 * What the caller may learn about the views that refer to a table. They all count, only those readable are named.
 */
final class TableDependents {

  private TableDependents() {
  }

  static List<String> visible(List<DependentView> views) {
    return views.stream()
        .filter(view -> SecurityUtils.getSubject()
            .isPermitted("rest:/datasource/" + view.datasource() + "/table/" + view.name() + ":GET"))
        .map(DependentView::reference)
        .toList();
  }

  static Response conflict(List<DependentView> views) {
    return Response.status(Response.Status.CONFLICT)
        .entity(ClientErrorDtos.getErrorMessage(Response.Status.CONFLICT, "TableHasDependentViews",
            visible(views).toArray(new String[0])).build())
        .build();
  }
}
