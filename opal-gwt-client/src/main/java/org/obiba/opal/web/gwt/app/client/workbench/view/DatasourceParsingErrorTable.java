/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.workbench.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.model.client.magma.DatasourceParsingErrorDto;
import org.obiba.opal.web.model.client.magma.DatasourceParsingErrorDto.ClientErrorDtoExtensions;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.ListView;

/**
 *
 */
public class DatasourceParsingErrorTable extends Table<DatasourceParsingErrorDto> {
  //
  // Static Variables
  //

  private static Translations translations = GWT.create(Translations.class);

  public DatasourceParsingErrorTable() {
    super();
    initTable();
  }

  public void setErrors(final ClientErrorDto errorDto) {
    setErrors(extractDatasourceParsingErrors(errorDto));
  }

  public void setErrors(final List<DatasourceParsingErrorDto> errors) {
    setDelegate(new Delegate<DatasourceParsingErrorDto>() {

      public void onRangeChanged(ListView<DatasourceParsingErrorDto> listView) {
        int start = listView.getRange().getStart();
        int length = listView.getRange().getLength();
        listView.setData(start, length, errors);
      }
    });

    setData(0, getPageSize(), errors);
    setDataSize(errors.size(), true);
    redraw();
  }

  @SuppressWarnings("unchecked")
  private List<DatasourceParsingErrorDto> extractDatasourceParsingErrors(ClientErrorDto dto) {
    List<DatasourceParsingErrorDto> datasourceParsingErrors = new ArrayList<DatasourceParsingErrorDto>();

    JsArray<DatasourceParsingErrorDto> errors = (JsArray<DatasourceParsingErrorDto>) dto.getExtension(ClientErrorDtoExtensions.errors);
    if(errors != null) {
      for(int i = 0; i < errors.length(); i++) {
        datasourceParsingErrors.add(errors.get(i));
      }

      sortBySheetAndRow(datasourceParsingErrors);
    }

    return datasourceParsingErrors;
  }

  private void sortBySheetAndRow(List<DatasourceParsingErrorDto> errors) {
    // sort alphabetically
    Collections.sort(errors, new Comparator<DatasourceParsingErrorDto>() {

      @Override
      public int compare(DatasourceParsingErrorDto e1, DatasourceParsingErrorDto e2) {
        int comp = e1.getArgumentsArray().get(0).compareTo(e2.getArgumentsArray().get(0));
        if(comp == 0) {
          comp = Integer.parseInt(e1.getArgumentsArray().get(1)) - Integer.parseInt(e2.getArgumentsArray().get(1));
        }
        return comp;
      }

    });
  }

  private void initTable() {
    setSelectionEnabled(false);
    addTableColumns();
  }

  private void addTableColumns() {
    addColumn(new TextColumn<DatasourceParsingErrorDto>() {
      @Override
      public String getValue(DatasourceParsingErrorDto dto) {
        return dto.getArgumentsArray().get(0);
      }
    }, translations.sheetLabel());

    addColumn(new TextColumn<DatasourceParsingErrorDto>() {
      @Override
      public String getValue(DatasourceParsingErrorDto dto) {
        return dto.getArgumentsArray().get(1);
      }
    }, translations.rowNumberLabel());

    addColumn(new TextColumn<DatasourceParsingErrorDto>() {
      @Override
      public String getValue(DatasourceParsingErrorDto dto) {
        // TODO: arguments in error message
        if(dto.getArgumentsArray().length() > 2) {

        }
        return translations.datasourceParsingErrorMap().get(dto.getKey());
      }
    }, translations.errorLabel());

    addColumn(new TextColumn<DatasourceParsingErrorDto>() {
      @Override
      public String getValue(DatasourceParsingErrorDto dto) {
        return dto.getArgumentsArray().length() > 2 ? dto.getArgumentsArray().get(2) : "";
      }
    }, translations.tableLabel());

    addColumn(new TextColumn<DatasourceParsingErrorDto>() {
      @Override
      public String getValue(DatasourceParsingErrorDto dto) {
        return dto.getArgumentsArray().length() > 3 ? dto.getArgumentsArray().get(3) : "";
      }
    }, translations.variableLabel());

    addColumn(new TextColumn<DatasourceParsingErrorDto>() {
      @Override
      public String getValue(DatasourceParsingErrorDto dto) {
        return dto.getArgumentsArray().length() > 4 ? dto.getArgumentsArray().get(4) : "";
      }
    }, translations.itemLabel());
  }

}
