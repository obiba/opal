/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
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
import org.obiba.magma.ValueView;
import org.obiba.magma.js.views.JavascriptClause;
import org.obiba.magma.support.ValueTableReference;
import org.obiba.magma.support.ValueTableWrapper;
import org.obiba.magma.views.JoinTable;
import org.obiba.magma.views.SelectClause;
import org.obiba.magma.views.View;
import org.obiba.magma.views.View.Builder;
import org.obiba.magma.views.support.NoneClause;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.JavaScriptViewDto;
import org.obiba.opal.web.model.Magma.TableDto;
import org.obiba.opal.web.model.Magma.ViewDto;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * An implementation of {@Code ViewDtoExtension} for {@code View} instances that have a {@code SelectClause}.
 */
@Component
public class JavaScriptViewDtoExtension extends TableViewDtoExtension {

  @Override
  public boolean isExtensionOf(@NotNull ViewDto viewDto) {
    return viewDto.hasExtension(JavaScriptViewDto.view);
  }

  @Override
  public boolean isDtoOf(@NotNull ValueView view) {
    return view instanceof View && ((View) view).getListClause() instanceof NoneClause;
  }

  @Override
  public ValueView fromDto(ViewDto viewDto) {
    Builder viewBuilder = getBuilder(viewDto);
    JavaScriptViewDto jsDto = viewDto.getExtension(JavaScriptViewDto.view);

    if (jsDto.hasSelect()) {
      SelectClause selectClause = new JavascriptClause(jsDto.getSelect());
      viewBuilder.select(selectClause);
    }

    return viewBuilder.build();
  }

  @Override
  public ViewDto asDto(ValueView view) {
    ViewDto.Builder viewDtoBuilder = ViewDto.newBuilder();
    viewDtoBuilder.setDatasourceName(view.getDatasource().getName());
    viewDtoBuilder.setName(view.getName());
    View jsView = (View) view;
    if (jsView.getWhereClause() instanceof JavascriptClause) {
      viewDtoBuilder.setWhere(((JavascriptClause) jsView.getWhereClause()).getScript());
    }
    setFromTables(jsView, viewDtoBuilder);

    JavaScriptViewDto.Builder jsDtoBuilder = JavaScriptViewDto.newBuilder();
    if (jsView.getSelectClause() instanceof JavascriptClause) {
      jsDtoBuilder.setSelect(((JavascriptClause) jsView.getSelectClause()).getScript());
    }

    viewDtoBuilder.setExtension(JavaScriptViewDto.view, jsDtoBuilder.build());

    return viewDtoBuilder.build();
  }

  private void setFromTables(ValueTableWrapper view, ViewDto.Builder viewDtoBuilder) {
    ValueTable from = view.getWrappedValueTable();
    if (from instanceof JoinTable) {
      List<ValueTable> fromTables = ((JoinTable) from).getTables();
      for (ValueTable vt : fromTables) {
        if (hasTableAccess(vt)) viewDtoBuilder.addFrom(toStringReference(vt));
      }
      viewDtoBuilder.addAllInnerFrom(((JoinTable) from).getInnerTableReferences());
    } else {
      if (hasTableAccess(from)) viewDtoBuilder.addFrom(toStringReference(from));
    }
  }

  String toStringReference(ValueTable vt) {
    if (vt instanceof ValueTableReference) {
      return ((ValueTableReference) vt).getReference();
    }
    return vt.getDatasource().getName() + "." + vt.getName();
  }

  @Override
  public TableDto asTableDto(ViewDto viewDto, Magma.TableDto.Builder tableDtoBuilder) {
    throw new UnsupportedOperationException();
  }

  private boolean hasTableAccess(ValueTable vt) {
    return vt != null && vt.getDatasource() != null && MagmaEngine.get().hasDatasource(vt.getDatasource().getName()) &&
        MagmaEngine.get().getDatasource(vt.getDatasource().getName()).hasValueTable(vt.getName());
  }
}