/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.support;

import java.util.ArrayList;
import java.util.Collection;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.model.client.magma.DatasourceParsingErrorDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.shared.GWT;

public final class DatasourceParsingErrorDtos {

  private DatasourceParsingErrorDtos() {}

  private static final Translations translations = GWT.create(Translations.class);

  public static Collection<String> getErrors(ClientErrorDto errorDto) {
    return createErrorCollection(extractDatasourceParsingErrors(errorDto));
  }

  private static Collection<String> createErrorCollection(Iterable<DatasourceParsingErrorDto> errors) {
    Collection<String> errorCollection = new ArrayList<String>();
    for(DatasourceParsingErrorDto dto : errors) {
      errorCollection.add(getErrorMessage(dto));
    }

    return errorCollection;
  }

  @SuppressWarnings("unchecked")
  private static Iterable<DatasourceParsingErrorDto> extractDatasourceParsingErrors(ClientErrorDto dto) {
    Collection<DatasourceParsingErrorDto> datasourceParsingErrors = new ArrayList<DatasourceParsingErrorDto>();
    JsArray<DatasourceParsingErrorDto> errors = (JsArray<DatasourceParsingErrorDto>) dto
        .getExtension(DatasourceParsingErrorDto.ClientErrorDtoExtensions.errors);
    if(errors != null) {
      for(int i = 0; i < errors.length(); i++) {
        datasourceParsingErrors.add(errors.get(i));
      }
    }

    return datasourceParsingErrors;
  }

  private static String getErrorMessage(DatasourceParsingErrorDto dto) {
    if(!translations.datasourceParsingErrorMap().containsKey(dto.getKey())) {
      return dto.getDefaultMessage();
    }
    return TranslationsUtils
        .replaceArguments(translations.datasourceParsingErrorMap().get(dto.getKey()), dto.getArgumentsArray());
  }
}
