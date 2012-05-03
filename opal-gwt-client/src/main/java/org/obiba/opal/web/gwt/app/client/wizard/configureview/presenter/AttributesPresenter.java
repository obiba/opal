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
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import net.customware.gwt.presenter.client.EventBus;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.util.VariableDtos;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.AttributeUpdateEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.UpdateType;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.VariableListViewDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 *
 */
public class AttributesPresenter extends LocalizablesPresenter {

  /** The attribute name 'label'. Attribute names are not localised. */
  private static final String LABEL = "label";

  //
  // Instance Variables
  //

  @Inject
  private Provider<AttributeDialogPresenter> attributeDialogPresenterProvider;

  private ClickHandler addButtonClickHandler;

  private EditActionHandler editActionHandler;

  private DeleteActionHandler deleteActionHandler;

  private List<String> attributeNames;

  //
  // Constructors
  //

  @Inject
  public AttributesPresenter(final Display display, final EventBus eventBus) {
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
      if(!attributeDto.getName().equals(VariableDtos.SCRIPT_ATTRIBUTE) && (attributeDto.getLocale().equals(localeName) ||
          isAttributeWithNoLocale(attributeDto))) {
        localizables.add(new Localizable() {

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
    return (!attributeDto.hasLocale() || attributeDto.getLocale().equals("")) && attributeDto.hasValue();
  }

  @Override
  protected void afterViewDtoSet() {
    attributeNames = getUniqueAttributeNames();
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
    super.registerHandler(eventBus.addHandler(AttributeUpdateEvent.getType(), new AttributeUpdateEventHandler()));
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

  protected String getDeleteConfirmationTitle() {
    return "deleteAttribute";
  }

  protected String getDeleteConfirmationMessage() {
    return "confirmDeleteAttribute";
  }

  //
  // Methods
  //

  public void setVariableDto(VariableDto variableDto) {
    this.variableDto = variableDto;
  }

  private List<String> getUniqueAttributeNames() {
    SortedSet<String> attributeNames = new TreeSet<String>();
    VariableListViewDto variableListDto = (VariableListViewDto) viewDto.getExtension(VariableListViewDto.ViewDtoExtensions.view);
    for(VariableDto variable : JsArrays.toList(variableListDto.getVariablesArray())) {
      for(AttributeDto attribute : JsArrays.toList(variable.getAttributesArray())) {
        attributeNames.add(attribute.getName());
      }
    }
    attributeNames.add(LABEL); // The attribute name 'label' will always be available.
    return new ArrayList<String>(attributeNames);
  }

  /** Adds and sorts unique attribute names. */
  private void addUniqueAttributeName(String attributeName) {
    SortedSet<String> attributeNameSet = new TreeSet<String>(attributeNames);
    attributeNameSet.add(attributeName);
    attributeNames = new ArrayList<String>(attributeNameSet);
  }

  //
  // Inner Classes / Interfaces
  //

  class AddNewAttributeButtonHandler implements ClickHandler {
    //
    // ClickHandler Methods
    //

    @Override
    public void onClick(ClickEvent event) {
      AttributeDialogPresenter attributeDialogPresenter = attributeDialogPresenterProvider.get();
      attributeDialogPresenter.bind();
      attributeDialogPresenter.setViewDto(viewDto);
      if(attributeNames == null) attributeNames = getUniqueAttributeNames();
      attributeDialogPresenter.getDisplay().setNameDropdownList(attributeNames);
      attributeDialogPresenter.setAttributes(variableDto.getAttributesArray());
      attributeDialogPresenter.revealDisplay();
    }
  }

  class EditAttributeHandler implements EditActionHandler {

    @Override
    public void onEdit(Localizable localizable) {
      AttributeDialogPresenter attributeDialogPresenter = attributeDialogPresenterProvider.get();
      attributeDialogPresenter.bind();
      attributeDialogPresenter.setViewDto(viewDto);
      attributeDialogPresenter.setAttributeNameToDisplay(localizable.getName());
      attributeDialogPresenter.setAttributes(variableDto.getAttributesArray());
      attributeDialogPresenter.getDisplay().setNameDropdownList(attributeNames);
      attributeDialogPresenter.revealDisplay();
    }
  }

  class DeleteAttributeHandler implements DeleteActionHandler {

    @SuppressWarnings("unchecked")
    @Override
    public void onDelete(Localizable localizable) {
      JsArray<AttributeDto> updatedAttributesArray = (JsArray<AttributeDto>) JsArray.createArray();

      for(int attributeIndex = 0; attributeIndex < variableDto.getAttributesArray().length(); attributeIndex++) {
        AttributeDto attributeDto = variableDto.getAttributesArray().get(attributeIndex);

        if(!attributeDto.getName().equals(localizable.getName())) {
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
      if(event.getUpdateType().equals(UpdateType.ADD)) {
        addAttribute(event);
      } else if(event.getUpdateType().equals(UpdateType.EDIT)) {
        replaceAttribute(event);
      }
      AttributesPresenter.this.refreshTableData();
    }

    @SuppressWarnings("unchecked")
    private void addAttribute(AttributeUpdateEvent event) {
      if(variableDto.getAttributesArray() == null) {
        variableDto.setAttributesArray((JsArray<AttributeDto>) JsArray.createArray());
      }

      for(int attributeIndex = 0; attributeIndex < event.getAttributes().length(); attributeIndex++) {
        AttributeDto newAttribute = event.getAttributes().get(attributeIndex);
        variableDto.getAttributesArray().push(newAttribute);
        addUniqueAttributeName(newAttribute.getName());
      }
    }

    private void replaceAttribute(AttributeUpdateEvent event) {
      deleteAttributeListByName(event.getAttributes());
      for(int attributeIndex = 0; attributeIndex < event.getAttributes().length(); attributeIndex++) {
        AttributeDto updatedAttribute = event.getAttributes().get(attributeIndex);
        variableDto.getAttributesArray().push(updatedAttribute);
      }
    }

    private void deleteAttributeListByName(JsArray<AttributeDto> attributeDtos) {
      for(int i = 0; i < attributeDtos.length(); i++) {
        deleteAttributeByName(attributeDtos.get(i));
      }
    }

    private void deleteAttributeByName(AttributeDto attributeDto) {
      @SuppressWarnings("unchecked")
      JsArray<AttributeDto> result = (JsArray<AttributeDto>) JsArray.createArray();
      for(int attributeIndex = 0; attributeIndex < variableDto.getAttributesArray().length(); attributeIndex++) {
        AttributeDto attribute = variableDto.getAttributesArray().get(attributeIndex);
        if(!attribute.getName().equals(attributeDto.getName())) {
          result.push(attribute);
        }
      }
      variableDto.clearAttributesArray();
      variableDto.setAttributesArray(result);
    }
  }
}
