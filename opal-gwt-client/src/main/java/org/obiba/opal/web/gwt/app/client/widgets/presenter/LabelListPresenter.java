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

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.TextBox;
import com.google.inject.Inject;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;
import org.obiba.opal.web.gwt.app.client.validator.AbstractFieldValidator;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.opal.LocaleDto;

public class LabelListPresenter extends WidgetPresenter<LabelListPresenter.Display> {

  public interface Display extends WidgetDisplay {

    public void setLanguages(JsArray<LocaleDto> languages);

    public Map<String, TextBox> getLanguageLabelMap();

    public LocaleDto getBaseLanguage();

    public void displayAttributes(String attributeName, JsArray<AttributeDto> attributes);

    public void clearAttributes(); // i.e., clear the attribute values

  }

  private String attributeToDisplay;

  private JsArray<AttributeDto> attributes;

  private String datasourceName;

  @Inject
  public LabelListPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
  }

  @Override
  protected void onBind() {
  }

  private void getLanguages() {
    UriBuilder ub = UriBuilder.create().segment("datasource", datasourceName, "locales");
    ResourceRequestBuilderFactory.<JsArray<LocaleDto>>newBuilder().forResource(ub.build()).get()
        .withCallback(new ResourceCallback<JsArray<LocaleDto>>() {
          @Override
          public void onResource(Response response, JsArray<LocaleDto> resource) {
            @SuppressWarnings("unchecked")
            JsArray<LocaleDto> languages = (resource != null) ? resource : (JsArray<LocaleDto>) JsArray.createArray();

            // Add the 'no locale' locale to the list of locales.
            LocaleDto noLocaleDto = LocaleDto.create();
            noLocaleDto.setName("");
            languages.push(noLocaleDto);

            getDisplay().setLanguages(languages);
            updateFields();
          }
        }).send();
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  public void setAttributes(JsArray<AttributeDto> attributes) {
    this.attributes = attributes;
  }

  public void setAttributeToDisplay(String attributeToDisplay) {
    this.attributeToDisplay = attributeToDisplay;
  }

  public void updateFields() {
    getDisplay().clearAttributes();

    if(attributeToDisplay != null) {
      getDisplay().displayAttributes(attributeToDisplay, attributes);
    }
  }

  public class BaseLanguageTextRequiredValidator extends AbstractFieldValidator {

    public BaseLanguageTextRequiredValidator(String errorMessageKey) {
      super(errorMessageKey);
    }

    @Override
    protected boolean hasError() {
      LocaleDto baseLanguageLocaleDto = getDisplay().getBaseLanguage();
      String baseLanguageLabelValue = getDisplay().getLanguageLabelMap().get(baseLanguageLocaleDto.getName())
          .getValue();

      // Base language not required when no locale value is provided.
      String noLocaleValue = getDisplay().getLanguageLabelMap().get("").getValue();
      if(noLocaleValue != null && !noLocaleValue.equals("")) return false;

      if(baseLanguageLabelValue == null || baseLanguageLabelValue.equals("")) return true;
      return false;
    }

  }

  public void setDatasourceName(String datasourceName) {
    this.datasourceName = datasourceName;

    getLanguages();
  }

}
