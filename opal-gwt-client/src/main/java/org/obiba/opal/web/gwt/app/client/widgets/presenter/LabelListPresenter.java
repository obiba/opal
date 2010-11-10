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

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.validator.AbstractFieldValidator;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.opal.LocaleDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.TextBox;
import com.google.inject.Inject;

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
    ResourceRequestBuilderFactory.<JsArray<LocaleDto>> newBuilder().forResource("/datasource/" + datasourceName + "/locales").get().withCallback(new ResourceCallback<JsArray<LocaleDto>>() {
      @Override
      public void onResource(Response response, JsArray<LocaleDto> resource) {
        @SuppressWarnings("unchecked")
        JsArray<LocaleDto> languages = (resource != null) ? resource : (JsArray<LocaleDto>) JsArray.createArray();
        getDisplay().setLanguages(languages);
        getDisplay().displayAttributes(attributeToDisplay, attributes);
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
    getDisplay().displayAttributes(attributeToDisplay, attributes);
  }

  public class BaseLanguageTextRequiredValidator extends AbstractFieldValidator {

    public BaseLanguageTextRequiredValidator(String errorMessageKey) {
      super(errorMessageKey);
    }

    @Override
    protected boolean hasError() {
      LocaleDto baseLanguageLocaleDto = getDisplay().getBaseLanguage();
      String baseLanguageLabelValue = getDisplay().getLanguageLabelMap().get(baseLanguageLocaleDto.getName()).getValue();
      if(baseLanguageLabelValue == null || baseLanguageLabelValue.equals("")) return true;
      return false;
    }

  }

  public void setDatasourceName(String datasourceName) {
    this.datasourceName = datasourceName;

    getLanguages();
  }

}
