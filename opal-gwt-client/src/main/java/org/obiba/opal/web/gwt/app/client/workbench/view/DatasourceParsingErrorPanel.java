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
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.model.client.magma.DatasourceParsingErrorDto;
import org.obiba.opal.web.model.client.magma.DatasourceParsingErrorDto.ClientErrorDtoExtensions;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 *
 */
public class DatasourceParsingErrorPanel extends FlowPanel {
  //
  // Static Variables
  //

  private static Translations translations = GWT.create(Translations.class);

  public DatasourceParsingErrorPanel() {
  }

  public void setErrors(ClientErrorDto errorDto) {
    setErrors(extractDatasourceParsingErrors(errorDto));
  }

  private void setErrors(List<DatasourceParsingErrorDto> errors) {
    clear();
    AlertPanel.Builder builder = AlertPanel.newBuilder().error();
    for(DatasourceParsingErrorDto dto : errors) {
      builder.error(getErrorMessage(dto));
    }
    add(builder.build());
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
    }

    return datasourceParsingErrors;
  }

  private String getErrorMessage(DatasourceParsingErrorDto dto) {
    if(!translations.datasourceParsingErrorMap().containsKey(dto.getKey())) {
      return dto.getDefaultMessage();
    }
    return TranslationsUtils
        .replaceArguments(translations.datasourceParsingErrorMap().get(dto.getKey()), dto.getArgumentsArray());
  }

}
