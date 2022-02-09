/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.magma.view;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.js.views.JavascriptClause;
import org.obiba.magma.views.View;
import org.obiba.magma.views.WhereClause;
import org.obiba.opal.web.model.Magma;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for views based on tables.
 */
public abstract class TableViewDtoExtension implements ValueViewDtoExtension {

  protected View.Builder getBuilder(Magma.ViewDto viewDto) {
    List<ValueTable> fromTables = getFromTables(viewDto);
    View.Builder builder = View.Builder
        .newView(viewDto.getName(), fromTables);

    if (viewDto.getInnerFromCount() > 0) {
      builder.innerFrom(viewDto.getInnerFromList());
    }

    if (viewDto.hasWhere()) {
      WhereClause whereClause = new JavascriptClause(viewDto.getWhere());
      builder.where(whereClause);
    }
    return builder;
  }

  private List<ValueTable> getFromTables(@NotNull Magma.ViewDto viewDto) {
    List<ValueTable> fromTables = new ArrayList<>();
    for (int i = 0; i < viewDto.getFromCount(); i++) {
      String fromTable = viewDto.getFrom(i);
      fromTables.add(MagmaEngine.get().createReference(fromTable));
    }
    return fromTables;
  }

}
