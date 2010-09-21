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

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.js.views.JavascriptClause;
import org.obiba.magma.js.views.VariablesClause;
import org.obiba.magma.math.OutlierRemovingView;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.views.SelectClause;
import org.obiba.magma.views.View;
import org.obiba.magma.views.WhereClause;
import org.obiba.magma.views.View.Builder;
import org.obiba.opal.web.model.Magma.JavaScriptViewDto;
import org.obiba.opal.web.model.Magma.OutlierRemovingViewDto;
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

  private static final ViewDtoExtension[] EXTENSIONS = { new JavaScriptViewDtoExtension(), new VariableListViewDtoExtension(), new OutlierRemovingViewDtoExtension() };

  //
  // Utilities
  //

  public static View fromDto(String viewName, ViewDto viewDto) {
    View view = null;

    View.Builder builder = View.Builder.newView(viewName, (ValueTable[]) getFromTables(viewDto).toArray());
    for(ViewDtoExtension extension : EXTENSIONS) {
      if(extension.isExtensionOf(viewDto)) {
        view = extension.build(viewDto, builder);
      }
    }

    return view;
  }

  private static List<ValueTable> getFromTables(ViewDto viewDto) {
    List<ValueTable> fromTables = new ArrayList<ValueTable>();
    for(int i = 0; i < viewDto.getFromCount(); i++) {
      String fromTable = viewDto.getFrom(i);
      MagmaEngineTableResolver tableResolver = MagmaEngineTableResolver.valueOf(fromTable);
      Datasource ds = MagmaEngine.get().getDatasource(tableResolver.getDatasourceName());
      ValueTable vt = ds.getValueTable(tableResolver.getTableName());
      fromTables.add(vt);
    }
    return fromTables;
  }

  //
  // Inner Classes / Interfaces
  //

  static interface ViewDtoExtension {

    boolean isExtensionOf(ViewDto viewDto);

    View build(ViewDto viewDto, View.Builder viewBuilder);
  }

  static class JavaScriptViewDtoExtension implements ViewDtoExtension {

    public boolean isExtensionOf(final ViewDto viewDto) {
      return viewDto.hasExtension(JavaScriptViewDto.view);
    }

    public View build(ViewDto viewDto, Builder viewBuilder) {
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
  }

  static class VariableListViewDtoExtension implements ViewDtoExtension {

    public boolean isExtensionOf(final ViewDto viewDto) {
      return viewDto.hasExtension(VariableListViewDto.view);
    }

    public View build(ViewDto viewDto, Builder viewBuilder) {
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
  }

  static class OutlierRemovingViewDtoExtension implements ViewDtoExtension {

    public boolean isExtensionOf(ViewDto viewDto) {
      return viewDto.hasExtension(OutlierRemovingViewDto.view);
    }

    public View build(ViewDto viewDto, Builder viewBuilder) {
      // OutlierRemovingViewDto outlierDto = viewDto.getExtension(OutlierRemovingViewDto.view);

      // TODO: Create the OutlierRemovingView based on the dto.
      return new OutlierRemovingView();
    }
  }
}
