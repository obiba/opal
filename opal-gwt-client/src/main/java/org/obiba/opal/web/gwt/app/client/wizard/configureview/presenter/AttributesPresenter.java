/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import net.customware.gwt.presenter.client.EventBus;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.util.AttributeDtos;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.AttributeUpdateEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.UpdateType;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.VariableListViewDto;

import com.google.common.base.Strings;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 *
 */
public class AttributesPresenter extends LocalizablesPresenter {

  //
  // Instance Variables
  //

  @Inject
  private Provider<AttributeDialogPresenter> attributeDialogPresenterProvider;

  private ClickHandler addButtonClickHandler;

  private EditActionHandler editActionHandler;

  private DeleteActionHandler deleteActionHandler;

  private final Collection<String> uniqueNames = new TreeSet<String>();

  //
  // Constructors
  //

  @Inject
  public AttributesPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  //
  // LocalizablesPresenter Methods
  //

  @Override
  protected List<Localizable> getLocalizables(String localeName) {
    List<Localizable> localizables = new ArrayList<Localizable>();

    variableDto.setAttributesArray(JsArrays.toSafeArray(variableDto.getAttributesArray()));

    for(int i = 0; i < variableDto.getAttributesArray().length(); i++) {
      final AttributeDto attributeDto = variableDto.getAttributesArray().get(i);

      // Add attributes that belong to this locale as well as attributes without a locale.
      if(!attributeDto.getName().equals(AttributeDtos.SCRIPT_ATTRIBUTE) && (attributeDto.getLocale()
          .equals(localeName) || isAttributeWithNoLocale(attributeDto))) {
        localizables.add(new Localizable() {

          @Override
          public String getNamespace() {
            return attributeDto.getNamespace();
          }

          @Override
          public String getName() {
            return attributeDto.getName();
          }

          @Override
          public String getLabel() {
            return attributeDto.getValue();
          }
        });
      }
    }

    return localizables;
  }

  private boolean isAttributeWithNoLocale(AttributeDto attributeDto) {
    return Strings.isNullOrEmpty(attributeDto.getLocale()) && attributeDto.hasValue();
  }

  @Override
  protected void afterViewDtoSet() {
    loadUniqueAttributeNames();
  }

  @Override
  protected void bindDependencies() {
  }

  @Override
  protected void unbindDependencies() {
  }

  @Override
  protected void refreshDependencies() {
  }

  @Override
  protected void addEventHandlers() {
    super.addEventHandlers(); // call superclass method to register common handlers
    registerHandler(eventBus.addHandler(AttributeUpdateEvent.getType(), new AttributeUpdateEventHandler()));
  }

  @Override
  protected ClickHandler getAddButtonClickHandler() {
    if(addButtonClickHandler == null) {
      addButtonClickHandler = new AddNewAttributeButtonHandler();
    }
    return addButtonClickHandler;
  }

  @Override
  protected EditActionHandler getEditActionHandler() {
    if(editActionHandler == null) {
      editActionHandler = new EditAttributeHandler();
    }
    return editActionHandler;
  }

  @Override
  protected DeleteActionHandler getDeleteActionHandler() {
    if(deleteActionHandler == null) {
      deleteActionHandler = new DeleteAttributeHandler();
    }
    return deleteActionHandler;
  }

  @Override
  protected String getDeleteConfirmationTitle() {
    return "deleteAttribute";
  }

  @Override
  protected String getDeleteConfirmationMessage() {
    return "confirmDeleteAttribute";
  }

  @Override
  protected String getValueColumnName() {
    return translations.value();
  }

  //
  // Methods
  //

  @Override
  public void setVariableDto(VariableDto variableDto) {
    this.variableDto = variableDto;
  }

  private void loadUniqueAttributeNames() {
    VariableListViewDto variableListDto = (VariableListViewDto) viewDto
        .getExtension(VariableListViewDto.ViewDtoExtensions.view);
    for(VariableDto variable : JsArrays.toList(variableListDto.getVariablesArray())) {
      for(AttributeDto attribute : JsArrays.toList(variable.getAttributesArray())) {
        uniqueNames.add(attribute.getName());
      }
    }
    // always add known attributes
    for(Map.Entry<String, List<String>> entry : AttributeDtos.NAMESPACE_ATTRIBUTES.entrySet()) {
      uniqueNames.addAll(entry.getValue());
    }
  }

  private AttributeDto findAttribute(Localizable localizable) {
    return AttributeDtos.findAttribute(JsArrays.toList(variableDto.getAttributesArray()), localizable.getNamespace(),
        localizable.getName(), localizable.getLabel());
  }

  private void prepareAttributeDialog(Localizable localizable) {
    AttributeDialogPresenter attributeDialogPresenter = attributeDialogPresenterProvider.get();
    attributeDialogPresenter.bind();
    attributeDialogPresenter.setViewDto(viewDto);
    attributeDialogPresenter.setAttribute(localizable == null ? null : findAttribute(localizable));
    attributeDialogPresenter.setAttributes(variableDto.getAttributesArray());
    attributeDialogPresenter.getDisplay().setUniqueNames(uniqueNames);
    attributeDialogPresenter.revealDisplay();
  }

  //
  // Inner Classes / Interfaces
  //

  class AddNewAttributeButtonHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      prepareAttributeDialog(null);
    }
  }

  class EditAttributeHandler implements EditActionHandler {

    @Override
    public void onEdit(Localizable localizable) {
      prepareAttributeDialog(localizable);
    }
  }

  class DeleteAttributeHandler implements DeleteActionHandler {

    @SuppressWarnings("unchecked")
    @Override
    public void onDelete(Localizable localizable) {

      JsArray<AttributeDto> updatedAttributesArray = (JsArray<AttributeDto>) JsArray.createArray();

      for(int i = 0; i < variableDto.getAttributesArray().length(); i++) {
        AttributeDto attributeDto = variableDto.getAttributesArray().get(i);
        if(!(attributeDto.getNamespace().equals(Strings.nullToEmpty(localizable.getNamespace())) //
            && attributeDto.getName().equals(localizable.getName()))) {
          updatedAttributesArray.push(attributeDto);
        }
      }

      variableDto.clearAttributesArray();
      variableDto.setAttributesArray(updatedAttributesArray);
    }
  }

  class AttributeUpdateEventHandler implements AttributeUpdateEvent.Handler {

    @Override
    public void onAttributeUpdate(AttributeUpdateEvent event) {
      if(event.getUpdateType() == UpdateType.ADD) {
        addAttribute(event);
      } else if(event.getUpdateType() == UpdateType.EDIT) {
        replaceAttribute(event);
      }
      refreshTableData();
    }

    @SuppressWarnings("unchecked")
    private void addAttribute(AttributeUpdateEvent event) {
      if(variableDto.getAttributesArray() == null) {
        variableDto.setAttributesArray((JsArray<AttributeDto>) JsArray.createArray());
      }

      for(int i = 0; i < event.getAttributes().length(); i++) {
        AttributeDto newAttribute = event.getAttributes().get(i);
        variableDto.getAttributesArray().push(newAttribute);
        uniqueNames.add(newAttribute.getName());
      }
    }

    private void replaceAttribute(AttributeUpdateEvent event) {
      deleteAttributeListByName(event.getAttributes());
      for(int i = 0; i < event.getAttributes().length(); i++) {
        AttributeDto updatedAttribute = event.getAttributes().get(i);
        variableDto.getAttributesArray().push(updatedAttribute);
      }
    }

    private void deleteAttributeListByName(JsArray<AttributeDto> attributeDtos) {
      for(int i = 0; i < attributeDtos.length(); i++) {
        deleteAttributeByName(attributeDtos.get(i));
      }
    }

    @SuppressWarnings("unchecked")
    private void deleteAttributeByName(AttributeDto attributeDto) {
      JsArray<AttributeDto> result = (JsArray<AttributeDto>) JsArray.createArray();
      for(int i = 0; i < variableDto.getAttributesArray().length(); i++) {
        AttributeDto attribute = variableDto.getAttributesArray().get(i);
        if(!attribute.getNamespace().equals(attributeDto.getNamespace()) //
            && !attribute.getName().equals(attributeDto.getName())) {
          result.push(attribute);
        }
      }
      variableDto.clearAttributesArray();
      variableDto.setAttributesArray(result);
    }
  }
}
