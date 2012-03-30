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

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.ListDataProvider;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.model.client.magma.DatasourceParsingErrorDto;
import org.obiba.opal.web.model.client.magma.DatasourceParsingErrorDto.ClientErrorDtoExtensions;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

/**
 *
 */
public class DatasourceParsingErrorTable extends Table<DatasourceParsingErrorDto> {
  //
  // Static Variables
  //

  private static Translations translations = GWT.create(Translations.class);

  private ListDataProvider<DatasourceParsingErrorDto> dataProvider = new ListDataProvider<DatasourceParsingErrorDto>();

  public DatasourceParsingErrorTable() {
    super();
    initTable();
    dataProvider.addDataDisplay(this);
  }

  public void setErrors(final ClientErrorDto errorDto) {
    setErrors(extractDatasourceParsingErrors(errorDto));
  }

  public void setErrors(final List<DatasourceParsingErrorDto> errors) {
    dataProvider.setList(errors);
    dataProvider.refresh();
  }

  @SuppressWarnings("unchecked")
  private List<DatasourceParsingErrorDto> extractDatasourceParsingErrors(ClientErrorDto dto) {
    List<DatasourceParsingErrorDto> datasourceParsingErrors = new ArrayList<DatasourceParsingErrorDto>();

    JsArray<DatasourceParsingErrorDto> errors = (JsArray<DatasourceParsingErrorDto>) dto
        .getExtension(ClientErrorDtoExtensions.errors);
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
          comp = e1.getArgumentsArray().get(1).compareTo(e2.getArgumentsArray().get(1));
        }
        return comp;
      }

    });
  }

  private void initTable() {
    addTableColumns();
  }

  private void addTableColumns() {

    addColumn(new TextColumn<DatasourceParsingErrorDto>() {
      @Override
      public String getValue(DatasourceParsingErrorDto dto) {
        if(translations.datasourceParsingErrorMap().containsKey(dto.getKey()) == false) {
          return dto.getDefaultMessage();
        }
        String msg = translations.datasourceParsingErrorMap().get(dto.getKey());
        JsArrayString args = dto.getArgumentsArray();
        if(args != null) {
          for(int i = 0; i < args.length(); i++) {
            msg = msg.replace("{" + i + "}", args.get(i));
          }
        }
        return msg;
      }
    }, translations.errorLabel());

  }

}
