/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma.view;

import java.util.List;

import org.obiba.magma.ValueTable;
import org.obiba.magma.js.views.JavascriptClause;
import org.obiba.magma.support.ValueTableReference;
import org.obiba.magma.views.JoinTable;
import org.obiba.magma.views.SelectClause;
import org.obiba.magma.views.View;
import org.obiba.magma.views.View.Builder;
import org.obiba.magma.views.WhereClause;
import org.obiba.magma.views.support.NoneClause;
import org.obiba.opal.web.model.Magma.JavaScriptViewDto;
import org.obiba.opal.web.model.Magma.TableDto;
import org.obiba.opal.web.model.Magma.ViewDto;
import org.springframework.stereotype.Component;

/**
 * An implementation of {@Code ViewDtoExtension} for {@code View} instances that have a {@code SelectClause}.
 */
@Component
public class JavaScriptViewDtoExtension implements ViewDtoExtension {

  public boolean isExtensionOf(final ViewDto viewDto) {
    return viewDto.hasExtension(JavaScriptViewDto.view);
  }

  public boolean isDtoOf(View view) {
    return (view.getListClause() instanceof NoneClause);
  }

  public View fromDto(ViewDto viewDto, Builder viewBuilder) {
    JavaScriptViewDto jsDto = viewDto.getExtension(JavaScriptViewDto.view);

    if(jsDto.hasSelect()) {
      SelectClause selectClause = new JavascriptClause(jsDto.getSelect());
      viewBuilder.select(selectClause);
    }
    if(jsDto.hasWhere()) {
      WhereClause whereClause = new JavascriptClause(jsDto.getWhere());
      viewBuilder.where(whereClause);
    }

    return viewBuilder.build();
  }

  public ViewDto asDto(View view) {
    ViewDto.Builder viewDtoBuilder = ViewDto.newBuilder();
    viewDtoBuilder.setDatasourceName(view.getDatasource().getName());
    viewDtoBuilder.setName(view.getName());

    ValueTable from = view.getWrappedValueTable();
    if(from instanceof JoinTable) {
      List<ValueTable> fromTables = ((JoinTable) from).getTables();
      for(ValueTable vt : fromTables) {
        viewDtoBuilder.addFrom(toStringReference(vt));
      }
    } else {
      viewDtoBuilder.addFrom(toStringReference(from));
    }

    JavaScriptViewDto.Builder jsDtoBuilder = JavaScriptViewDto.newBuilder();
    if(view.getSelectClause() instanceof JavascriptClause) {
      jsDtoBuilder.setSelect(((JavascriptClause) view.getSelectClause()).getScript());
    }
    if(view.getWhereClause() instanceof JavascriptClause) {
      jsDtoBuilder.setWhere(((JavascriptClause) view.getWhereClause()).getScript());
    }

    viewDtoBuilder.setExtension(JavaScriptViewDto.view, jsDtoBuilder.build());

    return viewDtoBuilder.build();
  }

  String toStringReference(ValueTable vt) {
    if(vt instanceof ValueTableReference) {
      return ((ValueTableReference) vt).getReference();
    }
    return vt.getDatasource().getName() + "." + vt.getName();
  }

  @Override
  public TableDto asTableDto(ViewDto viewDto, org.obiba.opal.web.model.Magma.TableDto.Builder tableDtoBuilder) {
    throw new UnsupportedOperationException();
  }
}