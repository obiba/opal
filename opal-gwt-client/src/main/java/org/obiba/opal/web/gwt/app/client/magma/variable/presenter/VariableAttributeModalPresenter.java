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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.event.VariableRefreshEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.support.ErrorResponseCallback;
import org.obiba.opal.web.gwt.app.client.ui.LocalizedEditableText;
import org.obiba.opal.web.gwt.app.client.validator.AbstractFieldValidator;
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

import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.common.base.Strings;
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

  private List<JsArray<AttributeDto>> selectedItems;

  private List<String> locales;

  public enum Mode {
    UPDATE_SINGLE, UPDATE_MULTIPLE, CREATE
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

      VariableDto dto = getVariableDto();
      dto.setAttributesArray(getAttributesArray(dto));

      ResourceRequestBuilderFactory.newBuilder() //
          .forResource(UriBuilders.DATASOURCE_TABLE_VARIABLE.create()
              .build(table.getDatasourceName(), table.getName(), variable.getName())) //
          .withResourceBody(VariableDto.stringify(dto)) //
          .withCallback(Response.SC_OK, successCallback) //
          .withCallback(Response.SC_BAD_REQUEST, new ErrorResponseCallback(getView().asWidget())) //
          .put().send();
    }
  }

  private JsArray<AttributeDto> getAttributesArray(VariableDto dto) {
    List<AttributeDto> attributes = JsArrays.toList(dto.getAttributesArray());

    if(dialogMode == Mode.CREATE) return addNewAttribute(attributes);

    if(dialogMode == Mode.UPDATE_SINGLE) return updateSingleAttribute(attributes);

    return updateMultipleNamespace(attributes);

  }

  private JsArray<AttributeDto> updateMultipleNamespace(Iterable<AttributeDto> attributes) {
    // Update selected attributes namespace
    JsArray<AttributeDto> newAttributes = JsArrays.create().cast();
    for(AttributeDto attribute : attributes) {
      // if in selectedItems, change namespace fo all locales
      for(JsArray<AttributeDto> selectedAttributes : selectedItems) {
        if(attribute.getName().equals(selectedAttributes.get(0).getName())) {
          attribute.setNamespace(getView().getNamespaceSuggestBox().getText());
          break;
        }
      }

      newAttributes.push(attribute);
    }
    return newAttributes;
  }

  private JsArray<AttributeDto> updateSingleAttribute(List<AttributeDto> attributes) {
    // Update existing attribute
    JsArray<AttributeDto> newAttributes = JsArrays.create().cast();

    String originalName = selectedItems.get(0).get(0).getName();
    // Update values and namespace
    updateValuesAndNamespace(attributes, newAttributes, originalName);

    // Add other attributes
    for(AttributeDto attribute : attributes) {
      if(!attribute.getName().equals(getView().getName().getText())) {
        newAttributes.push(attribute);
      }
    }

    return newAttributes;
  }

  private void updateValuesAndNamespace(List<AttributeDto> attributes, JsArray<AttributeDto> newAttributes,
      String originalName) {
    for(LocalizedEditableText localizedText : getView().getLocalizedValues().getValue()) {
      AttributeDto a = null;
      for(AttributeDto attribute : attributes) {
        if(attribute.getName().equals(originalName) &&
            attribute.getLocale().equals(localizedText.getValue().getLocale())) {
          a = attribute;
          break;
        }
      }

      if(a == null) {
        newAttributes.push(getNewAttribute(localizedText));
      } else {
        a.setName(getView().getName().getText());
        a.setNamespace(getView().getNamespaceSuggestBox().getText());
        a.setValue(localizedText.getTextBox().getText());
        newAttributes.push(a);
      }
    }
  }

  private AttributeDto getNewAttribute(LocalizedEditableText localizedText) {
    AttributeDto newAttribute = AttributeDto.create();
    newAttribute.setName(getView().getName().getText());
    newAttribute.setNamespace(getView().getNamespaceSuggestBox().getText());
    newAttribute.setValue(localizedText.getTextBox().getText());
    newAttribute.setLocale(localizedText.getValue().getLocale());

    return newAttribute;
  }

  private JsArray<AttributeDto> addNewAttribute(List<AttributeDto> attributes) {
    JsArray<AttributeDto> newAttributes = JsArrays.create().cast();

    // Add other attributed
    for(AttributeDto attribute : attributes) {
      newAttributes.push(attribute);
    }

    // For each non-empty locale
    for(LocalizedEditableText localizedText : getView().getLocalizedValues().getValue()) {
      if(!localizedText.getTextBox().getText().isEmpty()) {
        newAttributes.push(getNewAttribute(localizedText));
      }
    }

    return newAttributes;
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

    if(variable.getAttributesArray() != null) {
      dto.setAttributesArray(variable.getAttributesArray());
    }

    if(variable.getCategoriesArray() != null) {
      dto.setCategoriesArray(variable.getCategoriesArray());
    }

    return dto;
  }

  public void initialize(TableDto tableDto, VariableDto variableDto) {
    table = tableDto;
    variable = variableDto;
    getView().setNamespaceSuggestions(variableDto);

    renderLocalizableTexts();
  }

  private void renderLocalizableTexts() {
    locales = new ArrayList<String>();
    getView().setUiHandlers(this);

    // Fetch locales and render categories
    ResourceRequestBuilderFactory.<JsArray<LocaleDto>>newBuilder()
        .forResource(UriBuilders.DATASOURCE_TABLE_LOCALES.create().build(table.getDatasourceName(), table.getName()))
        .get().withCallback(new ResourceCallback<JsArray<LocaleDto>>() {
      @Override
      public void onResource(Response response, JsArray<LocaleDto> resource) {
        for(LocaleDto localeDto : JsArrays.toList(resource)) {
          locales.add(localeDto.getName());
        }
        Collections.sort(locales);

        getView().getLocalizedValues().setValue(getLocalizedEditableTexts(null));
      }
    }).send();
  }

  private List<LocalizedEditableText> getLocalizedEditableTexts(@Nullable Map<String, String> localeTexts) {
    List<LocalizedEditableText> localizedValues = new ArrayList<LocalizedEditableText>();
    LocalizedEditableText noLocale = new LocalizedEditableText();

    if(localeTexts != null && localeTexts.containsKey("")) {
      noLocale.setValue(new LocalizedEditableText.LocalizedText("", localeTexts.get("")));
    } else {
      noLocale.setValue(new LocalizedEditableText.LocalizedText("", ""));
    }

    localizedValues.add(noLocale);

    for(String locale : locales) {
      if(!locale.isEmpty()) {
        LocalizedEditableText localized = new LocalizedEditableText();

        if(localeTexts != null && localeTexts.containsKey(locale)) {
          localized.setValue(new LocalizedEditableText.LocalizedText(locale, localeTexts.get(locale)));
        } else {
          localized.setValue(new LocalizedEditableText.LocalizedText(locale, ""));
        }
        localizedValues.add(localized);
      }
    }
    return localizedValues;
  }

  public void initialize(TableDto tableDto, VariableDto variableDto, final List<JsArray<AttributeDto>> selectedItems) {
    table = tableDto;
    variable = variableDto;
    this.selectedItems = selectedItems;
    getView().setNamespaceSuggestions(variableDto);

    if(selectedItems.size() == 1) {
      // Fetch locales and render categories
      ResourceRequestBuilderFactory.<JsArray<LocaleDto>>newBuilder()
          .forResource(UriBuilders.DATASOURCE_TABLE_LOCALES.create().build(table.getDatasourceName(), table.getName()))
          .get().withCallback(new ResourceCallback<JsArray<LocaleDto>>() {
        @Override
        public void onResource(Response response, JsArray<LocaleDto> resource) {
          List<LocaleDto> localeDtos = JsArrays.toList(resource);
          locales = new ArrayList<String>();
          for(LocaleDto localeDto : localeDtos) {
            locales.add(localeDto.getName());
          }

          // Add item locale if it contains more locale
          List<AttributeDto> attributes = JsArrays.toList(selectedItems.get(0));
          Map<String, String> localeTexts = new HashMap<String, String>();
          for(AttributeDto attribute : attributes) {
            boolean found = false;
            for(String locale : locales) {
              if(locale.equals(attribute.getLocale())) {
                found = true;
                break;
              }
            }
            if(!found) {
              locales.add(attribute.getLocale());
            }
            localeTexts.put(attribute.getLocale(), attribute.getValue());
          }

          Collections.sort(locales);

          getView().getLocalizedValues().setValue(getLocalizedEditableTexts(localeTexts));
          getView().getName().setText(attributes.get(0).getName());
          getView().getNamespaceSuggestBox().setValue(attributes.get(0).getNamespace());
        }
      }).send();
    } else {
      List<AttributeDto> attributes = JsArrays.toList(selectedItems.get(0));
      getView().getNamespaceSuggestBox().setValue(attributes.get(0).getNamespace());
    }

  }

  public void setDialogMode(Mode mode) {
    dialogMode = mode;
    getView().setDialogMode(mode);
  }

  private class AttributeValidationHandler extends ViewValidationHandler {

    private Set<FieldValidator> validators;

    @Override
    protected Set<FieldValidator> getValidators() {
      if(validators == null) {
        validators = new LinkedHashSet<FieldValidator>();

        if(dialogMode == Mode.UPDATE_SINGLE) {
          validators.add(
              new RequiredTextValidator(getView().getName(), "AttributeNameIsRequired", Display.FormField.NAME.name()));
        }

        if(dialogMode == Mode.CREATE) {
          // validate that namespace - name does not already exists
          validators.add(new UniqueAttributeNameValidator("AttributeAlreadyExists"));
        }
      }
      return validators;
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(id == null ? null : Display.FormField.valueOf(id), message);
    }
  }

  public interface Display extends PopupView, HasUiHandlers<VariableAttributeModalUiHandlers> {

    TakesValue<List<LocalizedEditableText>> getLocalizedValues();

    void setDialogMode(Mode mode);

    enum FormField {
      NAME,
      VALUE
    }

    void hideDialog();

    TextBox getNamespaceSuggestBox();

    HasText getName();

    void setNamespaceSuggestions(VariableDto variableDto);

    void showError(@Nullable FormField formField, String message);

    void clearErrors();

  }

  public class UniqueAttributeNameValidator extends AbstractFieldValidator {

    public UniqueAttributeNameValidator(String errorMessageKey) {
      super(errorMessageKey, Display.FormField.NAME.name());
    }

    @Override
    protected boolean hasError() {
      String safeNamespace = Strings.nullToEmpty(getView().getNamespaceSuggestBox().getText());
      String safeName = Strings.nullToEmpty(getView().getName().getText());

      // Using the same safeNamespace/safeName as an existing attribute is not permitted.
      JsArray<AttributeDto> attributesArray = JsArrays.toSafeArray(variable.getAttributesArray());
      for(int i = 0; i < attributesArray.length(); i++) {
        AttributeDto dto = attributesArray.get(i);
        if(safeNamespace.equals(dto.getNamespace()) && safeName.equals(dto.getName())) {
          return true;
        }
      }
      return false;
    }

  }

  private class AttributeSuccessCallback implements ResponseCodeCallback {

    @Override
    public void onResponseCode(Request request, Response response) {
      getView().hide();
      fireEvent(new VariableRefreshEvent());
    }

  }
}
