/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.variable.presenter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.event.VariableRefreshEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.support.ErrorResponseCallback;
import org.obiba.opal.web.gwt.app.client.ui.LocalizedEditableText;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.opal.LocaleDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public class VariableAttributeModalPresenter extends ModalPresenterWidget<VariableAttributeModalPresenter.Display>
    implements VariableAttributeModalUiHandlers {

  protected ValidationHandler validationHandler;

  private Mode dialogMode;

  private TableDto table;

  private VariableDto variable;

  private List<LocaleDto> locales;

  public enum Mode {
    UPDATE, CREATE
  }

  @Inject
  public VariableAttributeModalPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);

    getView().setUiHandlers(this);
  }

  @Override
  public void onBind() {
    validationHandler = new AttributeValidationHandler();
  }

  @Override
  public void cancel() {
    getView().hideDialog();
  }

  @Override
  public void save() {

    getView().clearErrors();

    if(validationHandler.validate()) {
      ResponseCodeCallback successCallback = new AttributeSuccessCallback();

      switch(dialogMode) {
        case CREATE:

          VariableDto dto = getVariableDto();
          dto.setAttributesArray(getAttributesArray(dto));

          ResourceRequestBuilderFactory.newBuilder() //
              .forResource(UriBuilders.DATASOURCE_TABLE_VARIABLE.create()
                  .build(table.getDatasourceName(), table.getName(), variable.getName())) //
              .withResourceBody(VariableDto.stringify(dto)) //
              .withCallback(Response.SC_OK, successCallback) //
              .withCallback(Response.SC_BAD_REQUEST, new ErrorResponseCallback(getView().asWidget())) //
              .put().send();
          break;
      }
    }
  }

  private JsArray<AttributeDto> getAttributesArray(VariableDto dto) {
    JsArray<AttributeDto> attributesArray = JsArrays.toSafeArray(dto.getAttributesArray());

    // For each non-empty locale
    for(LocalizedEditableText localizedText : getView().getLocalizedValues().getValue()) {
      if(!localizedText.getTextBox().getText().isEmpty()) {
        AttributeDto newAttribute = AttributeDto.create();
        newAttribute.setName(getView().getName().getText());
        newAttribute.setNamespace(getView().getNamespace());
        newAttribute.setValue(localizedText.getTextBox().getText());
        newAttribute.setLocale(localizedText.getValue().getLocale());
        attributesArray.push(newAttribute);
      }
    }
    return attributesArray;
  }

  private VariableDto getVariableDto() {
    VariableDto dto = VariableDto.create();
    dto.setLink(variable.getLink());
    dto.setIndex(variable.getIndex());
    dto.setIsNewVariable(variable.getIsNewVariable());
    dto.setParentLink(variable.getParentLink());
    dto.setName(variable.getName());
    dto.setEntityType(variable.getEntityType());
    dto.setValueType(variable.getValueType());
    dto.setIsRepeatable(variable.getIsRepeatable());
    dto.setUnit(variable.getUnit());
    dto.setReferencedEntityType(variable.getReferencedEntityType());
    dto.setMimeType(variable.getMimeType());
    dto.setOccurrenceGroup(variable.getOccurrenceGroup());
    dto.setAttributesArray(variable.getAttributesArray());

    if(variable.getCategoriesArray() != null) {
      dto.setCategoriesArray(variable.getCategoriesArray());
    }

    return dto;
  }

  public void initialize(TableDto tableDto, final VariableDto variableDto) {
    table = tableDto;
    variable = variableDto;

    locales = new ArrayList<LocaleDto>();
    getView().setUiHandlers(this);

    // Fetch locales and render categories
    ResourceRequestBuilderFactory.<JsArray<LocaleDto>>newBuilder()
        .forResource(UriBuilders.DATASOURCE_TABLE_LOCALES.create().build(table.getDatasourceName(), table.getName()))
        .get().withCallback(new ResourceCallback<JsArray<LocaleDto>>() {
      @Override
      public void onResource(Response response, JsArray<LocaleDto> resource) {
        locales = JsArrays.toList(JsArrays.toSafeArray(resource));
        getView().setNamespaceSuggestions(variableDto);

        List<LocalizedEditableText> localizedValues = new ArrayList<LocalizedEditableText>();
        LocalizedEditableText noLocale = new LocalizedEditableText();
        noLocale.setValue(new LocalizedEditableText.LocalizedText("", ""));
        localizedValues.add(noLocale);

        for(LocaleDto locale : locales) {
          LocalizedEditableText localized = new LocalizedEditableText();
          localized.setValue(new LocalizedEditableText.LocalizedText(locale.getName(), ""));
          localizedValues.add(localized);
        }

        getView().getLocalizedValues().setValue(localizedValues);
      }
    }).send();
  }

  public void setDialogMode(Mode mode) {
    dialogMode = mode;
  }

  private class AttributeValidationHandler extends ViewValidationHandler {

    private Set<FieldValidator> validators;

    @Override
    protected Set<FieldValidator> getValidators() {
      if(validators == null) {
        validators = new LinkedHashSet<FieldValidator>();

        validators.add(
            new RequiredTextValidator(getView().getName(), "AttributeNameIsRequired", Display.FormField.NAME.name()));
      }
      return validators;
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(Display.FormField.valueOf(id), message);
    }
  }

  public interface Display extends PopupView, HasUiHandlers<VariableAttributeModalUiHandlers> {

    String getNamespace();

    TakesValue<List<LocalizedEditableText>> getLocalizedValues();

    enum FormField {
      NAME,
      VALUE
    }

    void hideDialog();

    HasText getName();

    void setNamespaceSuggestions(VariableDto variableDto);

    void showError(@Nullable FormField formField, String message);

    void clearErrors();

  }

  private class AttributeSuccessCallback implements ResponseCodeCallback {

    @Override
    public void onResponseCode(Request request, Response response) {
      getView().hide();
      fireEvent(new VariableRefreshEvent());
    }

  }
}
