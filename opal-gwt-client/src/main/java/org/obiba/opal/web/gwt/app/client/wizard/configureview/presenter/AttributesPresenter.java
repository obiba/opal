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

import net.customware.gwt.presenter.client.EventBus;

import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.AttributeUpdateEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.UpdateType;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

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

  private VariableDto variableDto;

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
  protected void editLocalizable(Localizable localizable, String localeName) {
    // TODO: Show the "edit attribute" dialog.
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void deleteLocalizable(Localizable localizable, String localeName) {
    JsArray<AttributeDto> newAttributesArray = (JsArray<AttributeDto>) JsArray.createArray();

    for(int i = 0; i < variableDto.getAttributesArray().length(); i++) {
      AttributeDto attributeDto = variableDto.getAttributesArray().get(i);

      if(!attributeDto.getName().equals(localizable.getName()) || !attributeDto.getLocale().equals(localeName)) {
        newAttributesArray.push(attributeDto);
      }
    }

    variableDto.clearAttributesArray();
    variableDto.setAttributesArray(newAttributesArray);
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

  //
  // Methods
  //

  public void setVariableDto(VariableDto variableDto) {
    this.variableDto = variableDto;
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
      attributeDialogPresenter.revealDisplay();
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
