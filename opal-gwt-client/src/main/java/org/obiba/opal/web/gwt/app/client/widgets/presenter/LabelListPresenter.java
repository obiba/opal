/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.presenter;

import java.util.Map;

import org.obiba.opal.web.gwt.app.client.validator.AbstractFieldValidator;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.opal.LocaleDto;

import com.google.common.base.Strings;
import com.google.gwt.core.client.JsArray;
import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class LabelListPresenter extends PresenterWidget<LabelListPresenter.Display> {

  private JsArray<AttributeDto> attributes;

  private String datasourceName;

  private String namespace;

  private String name;

  @Inject
  public LabelListPresenter(EventBus eventBus, Display display) {
    super(eventBus, display);
  }

  private void getLanguages() {
    UriBuilder ub = UriBuilder.create().segment("datasource", datasourceName, "locales");
    ResourceRequestBuilderFactory.<JsArray<LocaleDto>>newBuilder().forResource(ub.build()).get()
        .withCallback(new ResourceCallback<JsArray<LocaleDto>>() {
          @Override
          public void onResource(Response response, JsArray<LocaleDto> resource) {
            @SuppressWarnings(
                "unchecked")
            JsArray<LocaleDto> languages = resource == null ? (JsArray<LocaleDto>) JsArray.createArray() : resource;

            // Add the 'no locale' locale to the list of locales.
            LocaleDto noLocaleDto = LocaleDto.create();
            noLocaleDto.setName("");
            languages.push(noLocaleDto);

            getView().setLanguages(languages);
            updateFields();
          }
        }).send();
  }

  public void setAttributes(JsArray<AttributeDto> attributes) {
    this.attributes = attributes;
  }

  public void setAttributeToDisplay(String namespace, String name) {
    this.namespace = namespace;
    this.name = name;
  }

  public void updateFields() {
    getView().clearAttributes();

    if(name != null) {
      getView().displayAttributes(namespace, name, attributes);
    }
  }

  public class BaseLanguageTextRequiredValidator extends AbstractFieldValidator {

    public BaseLanguageTextRequiredValidator(String errorMessageKey) {
      super(errorMessageKey);
    }

    @Override
    protected boolean hasError() {
      LocaleDto baseLanguageLocaleDto = getView().getBaseLanguage();
      String baseLanguageLabelValue = getView().getLanguageLabelMap().get(baseLanguageLocaleDto.getName()).getValue();

      // Base language not required when no locale value is provided.
      String noLocaleValue = getView().getLanguageLabelMap().get("").getValue();
      return Strings.isNullOrEmpty(noLocaleValue) && Strings.isNullOrEmpty(baseLanguageLabelValue);
    }

  }

  public void setDatasourceName(String datasourceName) {
    this.datasourceName = datasourceName;

    getLanguages();
  }

  public interface Display extends View {

    void setLanguages(JsArray<LocaleDto> languages);

    Map<String, TextBoxBase> getLanguageLabelMap();

    LocaleDto getBaseLanguage();

    void displayAttributes(String namespace, String name, JsArray<AttributeDto> attributes);

    void clearAttributes(); // i.e., clear the attribute values

    void setUseTextArea(boolean useTextArea);

  }
}
