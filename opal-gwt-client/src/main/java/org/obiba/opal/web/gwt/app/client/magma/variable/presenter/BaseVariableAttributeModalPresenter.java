/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.magma.variable.presenter;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.event.VariableRefreshEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.support.ErrorResponseCallback;
import org.obiba.opal.web.gwt.app.client.support.OpalSystemCache;
import org.obiba.opal.web.gwt.app.client.validator.*;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import javax.annotation.Nullable;
import java.util.*;

public class BaseVariableAttributeModalPresenter<V extends BaseVariableAttributeModalPresenter.Display>
    extends ModalPresenterWidget<V> implements VariableAttributeModalUiHandlers {

  public enum Mode {
    APPLY, UPDATE_SINGLE, UPDATE_MULTIPLE, DELETE, DELETE_SINGLE, CREATE
  }

  protected final OpalSystemCache opalSystemCache;

  private Mode dialogMode;

  protected TableDto table;

  protected final List<VariableDto> variables = new ArrayList<VariableDto>();

  protected List<JsArray<AttributeDto>> selectedItems;

  protected ValidationHandler validationHandler;

  protected String namespace;

  protected String name;

  protected Map<String, String> localizedTexts;

  protected List<String> locales;

  public BaseVariableAttributeModalPresenter(EventBus eventBus, V view, OpalSystemCache opalSystemCache) {
    super(eventBus, view);
    this.opalSystemCache = opalSystemCache;
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
  public void save(String namespace, String name, Map<String, String> localizedTexts) {
    this.namespace = namespace;
    this.name = name;
    this.localizedTexts = localizedTexts;
    getView().clearErrors();

    if (validationHandler.validate()) {
      ResponseCodeCallback successCallback = new AttributeSuccessCallback();

      JsArrayString variableDtos = JsArrays.create().cast();
      for (VariableDto variable : variables) {
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

  public void initialize(TableDto tableDto, VariableDto variableDto) {
    Collection<VariableDto> variableDtos = new ArrayList<VariableDto>();
    variableDtos.add(variableDto);

    initialize(tableDto, variableDtos);
  }

  public void initialize(TableDto tableDto, Collection<VariableDto> variableDtos) {
    table = tableDto;
    variables.addAll(variableDtos);
    selectedItems = null;
    renderLocalizableTexts();
  }

  public void initialize(TableDto tableDto, VariableDto variableDto, final List<JsArray<AttributeDto>> selectedItems) {
    table = tableDto;
    variables.add(variableDto);
    this.selectedItems = selectedItems;
  }

  void applySelectedItems() {
    if (selectedItems.size() == 1) {
      // Fetch locales and render categories
      opalSystemCache.requestLocales(new OpalSystemCache.LocalesHandler() {
        @Override
        public void onLocales(JsArrayString localesStr) {
          locales = JsArrays.toList(localesStr);

          // Add item locale if it contains more locale
          List<AttributeDto> attributes = JsArrays.toList(selectedItems.get(0));
          Map<String, String> localeTexts = new HashMap<String, String>();
          for (AttributeDto attribute : attributes) {
            boolean found = false;
            for (String locale : locales) {
              if (locale.equals(attribute.getLocale())) {
                found = true;
                break;
              }
            }
            if (!found) {
              locales.add(attribute.getLocale());
            }
            localeTexts.put(attribute.getLocale(), attribute.getValue());
          }
          if (!locales.contains("")) locales.add("");
          Collections.sort(locales);

          getView().setNamespace(attributes.get(0).getNamespace());
          getView().setName(attributes.get(0).getName());
          getView().setLocalizedTexts(localeTexts, locales);
        }
      });
    } else {
      List<AttributeDto> attributes = JsArrays.toList(selectedItems.get(0));
      getView().setNamespace(attributes.get(0).getNamespace());
    }
  }

  public void setDialogMode(Mode mode) {
    dialogMode = mode;
    getView().setDialogMode(mode);
  }

  public void setDialogMode(Mode mode, String name) {
    dialogMode = mode;
    getView().setDialogMode(mode);
    getView().setSpecificName(name);
  }

  public static native String stringify(JavaScriptObject obj)
  /*-{
    return $wnd.JSON.stringify(obj);
  }-*/;

  //
  // Private methods and classes
  //

  private void renderLocalizableTexts() {
    locales = Lists.newArrayList();

    // Fetch locales and render categories
    opalSystemCache.requestLocales(new OpalSystemCache.LocalesHandler() {
      @Override
      public void onLocales(JsArrayString localesStr) {
        locales = JsArrays.toList(localesStr);
        if (!locales.contains("")) locales.add("");
        Collections.sort(locales);
        getView().setLocalizedTexts(new HashMap<String, String>(), locales);
      }
    });
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

    if (variable.getCategoriesArray() != null) {
      dto.setCategoriesArray(variable.getCategoriesArray());
    }

    return dto;
  }

  private JsArray<AttributeDto> getAttributesArray(VariableDto dto) {
    List<AttributeDto> attributes = JsArrays.toList(dto.getAttributesArray());

    switch (dialogMode) {
      case APPLY: // fall through
      case CREATE:
        return addNewAttribute(attributes);

      case UPDATE_SINGLE:
        return updateSingleAttribute(attributes);

      case DELETE:
      case DELETE_SINGLE:
        return deleteMultipleAttributes(dto);

      default:
        return updateMultipleNamespace(attributes);
    }
  }

  private JsArray<CategoryDto> getCategoriesDtoArray(JsArray<CategoryDto> modalCategories) {
    JsArray<CategoryDto> categories = JsArrays.create().cast();
    for (CategoryDto categoryDto : JsArrays.toIterable(modalCategories)) {
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
    for (AttributeDto attributeDto : JsArrays.toIterable(attributesArray)) {
      AttributeDto attribute = AttributeDto.create();
      attribute.setName(attributeDto.getName());
      attribute.setNamespace(attributeDto.getNamespace());
      attribute.setValue(attributeDto.getValue());
      attribute.setLocale(attributeDto.getLocale());

      attributes.push(attribute);
    }
    return attributes;
  }

  private JsArray<AttributeDto> addNewAttribute(Iterable<AttributeDto> attributes) {
    JsArray<AttributeDto> newAttributes = JsArrays.create().cast();

    // Add other attributed
    for (AttributeDto attribute : attributes) {
      newAttributes.push(attribute);
    }

    // For each locale
    for (Map.Entry<String, String> entry : localizedTexts.entrySet()) {
      AttributeDto existingAttr = findAttribute(attributes, entry.getKey());
      if (existingAttr != null) {
        existingAttr.setValue(entry.getValue());
      } else {
        newAttributes.push(getNewAttribute(entry.getKey(), entry.getValue()));
      }
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
    for (AttributeDto attribute : attributes) {
      if (!attribute.getName().equals(originalName) || !attribute.getNamespace().equals(originalNamespace)) {
        newAttributes.push(attribute);
      }
    }

    return newAttributes;
  }

  private JsArray<AttributeDto> deleteMultipleAttributes(VariableDto dto) {
    JsArray<AttributeDto> newAttributes = JsArrays.create().cast();

    String specificValue = "";
    if (localizedTexts.size() == 1 && localizedTexts.containsKey(""))
      specificValue = localizedTexts.get("");

    for (AttributeDto attributeDto : JsArrays.toIterable(dto.getAttributesArray())) {
      if (!Strings.isNullOrEmpty(specificValue)) {
        // remove only attributes with specific non-localized values
        if (!(attributeDto.getNamespace().equals(namespace) && attributeDto.getName().equals(name)
        && Strings.isNullOrEmpty(attributeDto.getLocale()) && attributeDto.getValue().equals(specificValue)))
          newAttributes.push(attributeDto);
      }
      else if (!attributeDto.getNamespace().equals(namespace) || !attributeDto.getName().equals(name)) {
        // Add attribute if its not for the specified namespace or name
        newAttributes.push(attributeDto);
      }
    }

    return newAttributes;
  }

  private JsArray<AttributeDto> updateMultipleNamespace(Iterable<AttributeDto> attributes) {
    // Update selected attributes namespace
    JsArray<AttributeDto> newAttributes = JsArrays.create().cast();
    for (AttributeDto attribute : attributes) {
      // if in selectedItems, change namespace fo all locales
      for (JsArray<AttributeDto> selectedAttributes : selectedItems) {
        if (attribute.getName().equals(selectedAttributes.get(0).getName())) {
          attribute.setNamespace(namespace);
          break;
        }
      }

      newAttributes.push(attribute);
    }
    return newAttributes;
  }

  private void updateValuesAndNamespace(Iterable<AttributeDto> attributes, JsArray<AttributeDto> newAttributes,
                                        String originalName, String originalNamespace) {

    for (AttributeDto attribute : attributes) {
      // Find attribute to edit
      if (attribute.getName().equals(originalName) && attribute.getNamespace().equals(originalNamespace)) {
        for (Map.Entry<String, String> entry : localizedTexts.entrySet()) {
          newAttributes.push(getNewAttribute(entry.getKey(), entry.getValue()));
        }
        break;
      }
    }
  }

  private AttributeDto findAttribute(Iterable<AttributeDto> attributes, String locale) {
    for (AttributeDto attribute : attributes) {
      if (name.equals(attribute.getName()) && isSameNamespace(namespace, attribute) && isSameLocale(locale, attribute)) {
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

  private AttributeDto getNewAttribute(String locale, String value) {
    AttributeDto newAttribute = AttributeDto.create();
    newAttribute.setNamespace(namespace);
    newAttribute.setName(name);
    newAttribute.setLocale(locale);
    newAttribute.setValue(value);
    return newAttribute;
  }

  private class AttributeSuccessCallback implements ResponseCodeCallback {

    @Override
    public void onResponseCode(Request request, Response response) {
      getView().hide();
      if (dialogMode != null) fireEvent(new VariableRefreshEvent());
    }

  }

  private class AttributeValidationHandler extends ViewValidationHandler {

    private Set<FieldValidator> validators;

    @Override
    protected Set<FieldValidator> getValidators() {
      validators = new LinkedHashSet<FieldValidator>();

      if (dialogMode == Mode.UPDATE_MULTIPLE) {
        validators.add(new AttributeConflictValidator("AttributeConflictExists"));
      } else {
        validators.add(new ConditionValidator(hasValidNamespace(), "NamespaceCannotBeEmptyChars",
            Display.FormField.NAMESPACE.name()));
        validators
            .add(new RequiredTextValidator(hasValidName(), "AttributeNameIsRequired", Display.FormField.NAME.name()));

        if (dialogMode != Mode.DELETE && dialogMode != Mode.DELETE_SINGLE) {
          validators
              .add(new ConditionValidator(hasValue(), "AttributeValueIsRequired", Display.FormField.VALUE.name()));
        }
      }
      return validators;
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(id == null ? null : Display.FormField.valueOf(id), message);
    }

    private HasValue<Boolean> hasValue() {
      return new HasBooleanValue() {
        @Override
        public Boolean getValue() {
          if (localizedTexts == null) return false;
          for (Map.Entry<String, String> entry : localizedTexts.entrySet()) {
            String value = entry.getValue();
            if (!Strings.isNullOrEmpty(value) && !entry.getValue().trim().isEmpty()) {
              return true;
            }
          }

          return false;
        }
      };
    }

    private HasValue<Boolean> hasValidNamespace() {
      return new HasBooleanValue() {
        @Override
        public Boolean getValue() {
          return Strings.isNullOrEmpty(namespace) || !namespace.trim().isEmpty();
        }
      };
    }

    private HasText hasValidName() {
      return new HasText() {
        @Override
        public String getText() {
          return name;
        }

        @Override
        public void setText(String text) {

        }
      };
    }
  }

  public class AttributeConflictValidator extends AbstractFieldValidator {

    public AttributeConflictValidator(String errorMessageKey) {
      super(errorMessageKey);
    }

    @Override
    protected boolean hasError() {
      for (JsArray<AttributeDto> attributesArray : selectedItems) {
        String safeNamespace = Strings.nullToEmpty(namespace);
        String existingName = attributesArray.get(0).getName();

        for (VariableDto variable : variables) {
          // Using the same safeNamespace/safeName as an existing attribute is not permitted.
          for (AttributeDto attributeDto : JsArrays.toIterable(variable.getAttributesArray())) {
            if (safeNamespace.equals(attributeDto.getNamespace()) && existingName.equals(attributeDto.getName())) {
              return true;
            }
          }
        }
      }

      return false;
    }
  }

  public interface Display extends PopupView, HasUiHandlers<VariableAttributeModalUiHandlers> {

    void setDialogMode(Mode mode);

    enum FormField {
      NAMESPACE,
      NAME,
      VALUE
    }

    void hideDialog();

    void showError(@Nullable FormField formField, String message);

    void setNamespace(String namespace);

    void setName(String name);

    void setSpecificName(String name);

    void setLocalizedTexts(Map<String, String> localizedTexts, List<String> locales);

    void clearErrors();

  }

}
