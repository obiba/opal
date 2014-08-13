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
import java.util.Collection;
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
import org.obiba.opal.web.gwt.app.client.validator.ConditionValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.HasBooleanValue;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.opal.LocaleDto;

import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.common.base.Strings;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public class VariableAttributeModalPresenter extends ModalPresenterWidget<VariableAttributeModalPresenter.Display>
    implements VariableAttributeModalUiHandlers {

  protected ValidationHandler validationHandler;

  private Mode dialogMode;

  private TableDto table;

  private final List<VariableDto> variables = new ArrayList<VariableDto>();

  private List<JsArray<AttributeDto>> selectedItems;

  private List<String> locales;

  public enum Mode {
    APPLY, UPDATE_SINGLE, UPDATE_MULTIPLE, DELETE, CREATE
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

      JsArrayString variableDtos = JsArrays.create().cast();
      for(VariableDto variable : variables) {
        VariableDto dto = getVariableDto(variable);
        variableDtos.push(VariableDto.stringify(dto));
      }

      UriBuilder uriBuilder = table.hasViewLink()
          ? UriBuilders.DATASOURCE_VIEW_VARIABLES.create()
          : UriBuilders.DATASOURCE_TABLE_VARIABLES.create();

      ResourceRequestBuilderFactory.newBuilder() //
          .forResource(uriBuilder.build(table.getDatasourceName(), table.getName())) //
          .withResourceBody("[" + variableDtos.toString() + "]") //
          .withCallback(Response.SC_OK, successCallback) //
          .withCallback(Response.SC_BAD_REQUEST, new ErrorResponseCallback(getView().asWidget())) //
          .post().send();

    }
  }

  private JsArray<AttributeDto> getAttributesArray(VariableDto dto) {
    List<AttributeDto> attributes = JsArrays.toList(dto.getAttributesArray());

    switch(dialogMode) {
      case APPLY: // fall through
      case CREATE:
        return addNewAttribute(attributes);

      case UPDATE_SINGLE:
        return updateSingleAttribute(attributes);

      case DELETE:
        return deleteMultipleAttributes(dto);

      default:
        return updateMultipleNamespace(attributes);
    }
  }

  private JsArray<AttributeDto> deleteMultipleAttributes(VariableDto dto) {
    JsArray<AttributeDto> newAttributes = JsArrays.create().cast();

    for(AttributeDto attributeDto : JsArrays.toIterable(dto.getAttributesArray())) {
      // Add attribute if its not for the specified namespace or name
      if(!attributeDto.getNamespace().equals(getView().getNamespaceSuggestBox().getText()) ||
          !attributeDto.getName().equals(getView().getName().getText())) {
        newAttributes.push(attributeDto);
      }
    }

    return newAttributes;
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
    String originalNamespace = selectedItems.get(0).get(0).getNamespace();

    // Update values and namespace
    updateValuesAndNamespace(attributes, newAttributes, originalName, originalNamespace);

    // Add other attributes
    for(AttributeDto attribute : attributes) {
      if(!attribute.getName().equals(originalName) || !attribute.getNamespace().equals(originalNamespace)) {
        newAttributes.push(attribute);
      }
    }

    return newAttributes;
  }

  private void updateValuesAndNamespace(Iterable<AttributeDto> attributes, JsArray<AttributeDto> newAttributes,
      String originalName, String originalNamespace) {

    for(AttributeDto attribute : attributes) {
      // Find attribute to edit
      if(attribute.getName().equals(originalName) && attribute.getNamespace().equals(originalNamespace)) {
        for(LocalizedEditableText localizedText : getView().getLocalizedValues().getValue()) {
          newAttributes.push(getNewAttribute(localizedText));
        }
        break;
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

  private JsArray<AttributeDto> addNewAttribute(Iterable<AttributeDto> attributes) {
    JsArray<AttributeDto> newAttributes = JsArrays.create().cast();

    // Add other attributed
    for(AttributeDto attribute : attributes) {
      newAttributes.push(attribute);
    }

    // For each non-empty locale
    for(LocalizedEditableText localizedText : getView().getLocalizedValues().getValue()) {
      if(!localizedText.getTextBox().getText().isEmpty()) {
        AttributeDto existingAttr = findAttribute(attributes, localizedText);
        if(existingAttr != null) {
          existingAttr.setValue(localizedText.getTextBox().getText());
        } else {
          newAttributes.push(getNewAttribute(localizedText));
        }
      }
    }

    return newAttributes;
  }

  private AttributeDto findAttribute(Iterable<AttributeDto> attributes, LocalizedEditableText localizedText) {
    String name = getView().getName().getText().trim();
    String namespace = getView().getNamespaceSuggestBox().getText().trim();
    String locale = localizedText.getValue().getLocale().trim();

    for(AttributeDto attribute : attributes) {
      if(name.equals(attribute.getName()) && isSameNamespace(namespace, attribute) && isSameLocale(locale, attribute)) {
        return attribute;
      }
    }

    return null;
  }

  private boolean isSameLocale(String locale, AttributeDto attribute) {
    return locale.isEmpty() && !attribute.hasLocale() || attribute.hasLocale() && locale.equals(attribute.getLocale());
  }

  private boolean isSameNamespace(String namespace, AttributeDto attribute) {
    return namespace.isEmpty() && !attribute.hasNamespace() ||
        attribute.hasNamespace() && namespace.equals(attribute.getNamespace());
  }

  private VariableDto getVariableDto(VariableDto variable) {
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
    dto.setIndex(variable.getIndex());

    dto.setAttributesArray(getAttributesArray(variable));
    dto.setCategoriesArray(getCategoriesDtoArray(variable.getCategoriesArray()));

    if(variable.getCategoriesArray() != null) {
      dto.setCategoriesArray(variable.getCategoriesArray());
    }

    return dto;
  }

  private JsArray<CategoryDto> getCategoriesDtoArray(JsArray<CategoryDto> modalCategories) {
    JsArray<CategoryDto> categories = JsArrays.create().cast();
    for(CategoryDto categoryDto : JsArrays.toIterable(modalCategories)) {
      CategoryDto category = CategoryDto.create();
      category.setName(categoryDto.getName());
      category.setIsMissing(categoryDto.getIsMissing());
      category.setAttributesArray(getAttributesDtoArray(categoryDto.getAttributesArray()));

      categories.push(category);
    }
    return categories;
  }

  private JsArray<AttributeDto> getAttributesDtoArray(JsArray<AttributeDto> attributesArray) {
    JsArray<AttributeDto> attributes = JsArrays.create().cast();
    for(AttributeDto attributeDto : JsArrays.toIterable(attributesArray)) {
      AttributeDto attribute = AttributeDto.create();
      attribute.setName(attributeDto.getName());
      attribute.setNamespace(attributeDto.getNamespace());
      attribute.setValue(attributeDto.getValue());
      attribute.setLocale(attributeDto.getLocale());

      attributes.push(attribute);
    }
    return attributes;
  }

  public void initialize(TableDto tableDto, VariableDto variableDto) {
    Collection<VariableDto> variableDtos = new ArrayList<VariableDto>();
    variableDtos.add(variableDto);

    initialize(tableDto, variableDtos);
  }

  public void initialize(TableDto tableDto, Collection<VariableDto> variableDtos) {
    table = tableDto;
    variables.addAll(variableDtos);
    getView().setNamespaceSuggestions(variables);

    renderLocalizableTexts();
  }

  private void renderLocalizableTexts() {
    locales = new ArrayList<String>();

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
    variables.add(variableDto);
    this.selectedItems = selectedItems;
    getView().setNamespaceSuggestions(variables);

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

  public static native String stringify(JavaScriptObject obj)
  /*-{
      return $wnd.JSON.stringify(obj);
  }-*/;

  private class AttributeValidationHandler extends ViewValidationHandler {

    private Set<FieldValidator> validators;

    @Override
    protected Set<FieldValidator> getValidators() {
      validators = new LinkedHashSet<FieldValidator>();

      if(dialogMode == Mode.UPDATE_MULTIPLE) {
        validators.add(new AttributeConflictValidator("AttributeConflictExists"));
      } else {
        validators.add(new ConditionValidator(hasValidNamespace(getView().getNamespaceSuggestBox().getText()),
            "NamespaceCannotBeEmptyChars", Display.FormField.NAMESPACE.name()));
        validators.add(
            new RequiredTextValidator(getView().getName(), "AttributeNameIsRequired", Display.FormField.NAME.name()));

        if(dialogMode != Mode.DELETE) {
          validators.add(
              new ConditionValidator(hasValue(getView().getLocalizedValues().getValue()), "AttributeValueIsRequired",
                  Display.FormField.VALUE.name()));
        }
        if(dialogMode == Mode.CREATE) validators.add(new UniqueAttributeNameValidator("AttributeAlreadyExists"));
      }
      return validators;
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(id == null ? null : Display.FormField.valueOf(id), message);
    }

    private HasValue<Boolean> hasValue(final Iterable<LocalizedEditableText> localizedTexts) {
      return new HasBooleanValue() {
        @Override
        public Boolean getValue() {
          for(LocalizedEditableText localizedText : localizedTexts) {
            if(!localizedText.getTextBox().getText().trim().isEmpty()) {
              return true;
            }
          }

          return false;
        }
      };
    }

    private HasValue<Boolean> hasValidNamespace(final String namespace) {
      return new HasBooleanValue() {
        @Override
        public Boolean getValue() {
          return Strings.isNullOrEmpty(namespace) || namespace.trim().length() > 0;
        }
      };
    }
  }

  public interface Display extends PopupView, HasUiHandlers<VariableAttributeModalUiHandlers> {

    TakesValue<List<LocalizedEditableText>> getLocalizedValues();

    void setDialogMode(Mode mode);

    enum FormField {
      NAMESPACE,
      NAME,
      VALUE
    }

    void hideDialog();

    TextBox getNamespaceSuggestBox();

    HasText getName();

    void setNamespaceSuggestions(List<VariableDto> variableDto);

    void showError(@Nullable FormField formField, String message);

    void clearErrors();

  }

  public class UniqueAttributeNameValidator extends AbstractFieldValidator {

    public UniqueAttributeNameValidator(String errorMessageKey) {
      super(errorMessageKey, Display.FormField.NAME.name());
    }

    @Override
    protected boolean hasError() {
      for(VariableDto variableDto : variables) {
        String safeNamespace = Strings.nullToEmpty(getView().getNamespaceSuggestBox().getText());
        String safeName = Strings.nullToEmpty(getView().getName().getText());

        // Using the same safeNamespace/safeName as an existing attribute is not permitted.
        for(AttributeDto attributeDto : JsArrays.toIterable(variableDto.getAttributesArray())) {
          if(safeNamespace.equals(attributeDto.getNamespace()) && safeName.equals(attributeDto.getName())) {
            return true;
          }
        }
      }

      return false;
    }
  }

  public class AttributeConflictValidator extends AbstractFieldValidator {

    public AttributeConflictValidator(String errorMessageKey) {
      super(errorMessageKey);
    }

    @Override
    protected boolean hasError() {
      for(JsArray<AttributeDto> attributesArray : selectedItems) {
        String safeNamespace = Strings.nullToEmpty(getView().getNamespaceSuggestBox().getText());
        String existingName = attributesArray.get(0).getName();

        for(VariableDto variable : variables) {
          // Using the same safeNamespace/safeName as an existing attribute is not permitted.
          for(AttributeDto attributeDto : JsArrays.toIterable(variable.getAttributesArray())) {
            if(safeNamespace.equals(attributeDto.getNamespace()) && existingName.equals(attributeDto.getName())) {
              return true;
            }
          }
        }
      }

      return false;
    }
  }

  private class AttributeSuccessCallback implements ResponseCodeCallback {

    @Override
    public void onResponseCode(Request request, Response response) {
      getView().hide();
      if(dialogMode != null) fireEvent(new VariableRefreshEvent());
    }

  }
}
