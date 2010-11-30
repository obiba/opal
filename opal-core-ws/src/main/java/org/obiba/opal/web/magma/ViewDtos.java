/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.js.views.JavascriptClause;
import org.obiba.magma.js.views.VariablesClause;
import org.obiba.magma.support.ValueTableReference;
import org.obiba.magma.views.JoinTable;
import org.obiba.magma.views.SelectClause;
import org.obiba.magma.views.View;
import org.obiba.magma.views.WhereClause;
import org.obiba.magma.views.View.Builder;
import org.obiba.magma.views.support.NoneClause;
import org.obiba.opal.web.model.Magma.JavaScriptViewDto;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.obiba.opal.web.model.Magma.VariableListViewDto;
import org.obiba.opal.web.model.Magma.ViewDto;

/**
 * Utilities for handling View Dtos.
 */
public final class ViewDtos {
  //
  // Constants
  //

  private static final ViewDtoExtension[] EXTENSIONS = { new JavaScriptViewDtoExtension(), new VariableListViewDtoExtension(), };

  //
  // Utilities
  //

  public static View fromDto(String viewName, ViewDto viewDto) {
    View view = null;

    List<ValueTable> fromTables = getFromTables(viewDto);

    View.Builder builder = View.Builder.newView(viewName, (ValueTable[]) fromTables.toArray(new ValueTable[fromTables.size()]));
    for(ViewDtoExtension extension : EXTENSIONS) {
      if(extension.isExtensionOf(viewDto)) {
        view = extension.fromDto(viewDto, builder);
      }
    }

    return view;
  }

  public static ViewDto asDto(View view) {
    for(ViewDtoExtension extension : EXTENSIONS) {
      if(extension.isDtoOf(view)) {
        return extension.asDto(view);
      }
    }
    throw new RuntimeException("Unknown view type");
  }

  private static List<ValueTable> getFromTables(ViewDto viewDto) {
    List<ValueTable> fromTables = new ArrayList<ValueTable>();
    for(int i = 0; i < viewDto.getFromCount(); i++) {
      String fromTable = viewDto.getFrom(i);
      ValueTable vt = new ValueTableReference(fromTable);
      fromTables.add(vt);
    }
    return fromTables;
  }

  private static String toStringReference(ValueTable vt) {
    return vt.getDatasource().getName() + "." + vt.getName();
  }

  //
  // Inner Classes / Interfaces
  //

  static interface ViewDtoExtension {

    boolean isExtensionOf(ViewDto viewDto);

    boolean isDtoOf(View view);

    View fromDto(ViewDto viewDto, View.Builder viewBuilder);

    ViewDto asDto(View view);
  }

  static class JavaScriptViewDtoExtension implements ViewDtoExtension {

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
  }

  static class VariableListViewDtoExtension implements ViewDtoExtension {

    public boolean isExtensionOf(final ViewDto viewDto) {
      return viewDto.hasExtension(VariableListViewDto.view);
    }

    public boolean isDtoOf(View view) {
      return (!(view.getListClause() instanceof NoneClause));
    }

    public View fromDto(ViewDto viewDto, Builder viewBuilder) {
      VariableListViewDto listDto = viewDto.getExtension(VariableListViewDto.view);

      if(listDto.hasWhere()) {
        WhereClause whereClause = new JavascriptClause(listDto.getWhere());
        viewBuilder.where(whereClause);
      }

      Set<Variable> variables = new LinkedHashSet<Variable>();
      for(VariableDto variableDto : listDto.getVariablesList()) {
        variables.add(Dtos.fromDto(variableDto));
      }

      VariablesClause listClause = new VariablesClause();
      listClause.setVariables(variables);
      viewBuilder.list(listClause);

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

      VariableListViewDto.Builder listDtoBuilder = VariableListViewDto.newBuilder();
      for(Variable v : view.getVariables()) {
        listDtoBuilder.addVariables(Dtos.asDto(v));
      }
      if(view.getWhereClause() instanceof JavascriptClause) {
        listDtoBuilder.setWhere(((JavascriptClause) view.getWhereClause()).getScript());
      }

      viewDtoBuilder.setExtension(VariableListViewDto.view, listDtoBuilder.build());

      return viewDtoBuilder.build();
    }
  }
}
