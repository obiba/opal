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
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.AttributeUpdateEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.UpdateType;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.VariableListViewDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.inject.Inject;

/**
 *
 */
public class AttributesPresenter extends LocalizablesPresenter {
  //
  // Instance Variables
  //

  @Inject
  private AttributeDialogPresenter attributeDialogPresenter;

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
    attributeNames = getUniqueAttributeNames();
  }

  //
  // LocalizablesPresenter Methods
  //

  @Override
  protected List<Localizable> getLocalizables(String localeName) {
    List<Localizable> localizables = new ArrayList<Localizable>();

    for(int i = 0; i < variableDto.getAttributesArray().length(); i++) {
      final AttributeDto attributeDto = variableDto.getAttributesArray().get(i);

      if(attributeDto.getLocale().equals(localeName)) {
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

  @Override
  protected void bindDependencies() {
    attributeDialogPresenter.bind();
  }

  @Override
  protected void unbindDependencies() {
    attributeDialogPresenter.unbind();
  }

  @Override
  protected void refreshDependencies() {
    attributeDialogPresenter.refreshDisplay();
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
    return new ArrayList<String>(attributeNames);
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
      // Each time the dialog is closed (hidden), it is unbound. So we need to rebind it each time we display it.
      attributeDialogPresenter.bind();
      attributeDialogPresenter.getDisplay().setNameDropdownList(attributeNames);
      attributeDialogPresenter.revealDisplay();
    }
  }

  class EditAttributeHandler implements EditActionHandler {

    @Override
    public void onEdit(Localizable localizable) {
      attributeDialogPresenter.setAttributeNameToDisplay(localizable.getName());

      // Each time the dialog is closed (hidden), it is unbound. So we need to rebind it each time we display it.
      attributeDialogPresenter.bind();
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
        attributeNames.add(newAttribute.getName());
      }
    }

    private void replaceAttribute(AttributeUpdateEvent event) {
      for(int attributeIndex = 0; attributeIndex < event.getAttributes().length(); attributeIndex++) {
        AttributeDto updatedAttribute = event.getAttributes().get(attributeIndex);

        int originalAttributeIndex = findOriginalAttribute(updatedAttribute);
        if(originalAttributeIndex != -1) {
          variableDto.getAttributesArray().set(originalAttributeIndex, updatedAttribute);
        }
      }
    }

    private int findOriginalAttribute(AttributeDto updatedAttribute) {
      for(int attributeIndex = 0; attributeIndex < variableDto.getAttributesArray().length(); attributeIndex++) {
        AttributeDto attribute = variableDto.getAttributesArray().get(attributeIndex);
        if(attribute.getName().equals(updatedAttribute.getName()) && attribute.getLocale() != null && attribute.getLocale().equals(updatedAttribute.getLocale())) {
          return attributeIndex;
        }
      }
      return -1; // not found
    }
  }
}
