/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter;

import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.AttributeUpdateEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.UpdateType;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

/**
 *
 */
public class AttributesPresenter extends PresenterWidget<AttributesPresenter.Display> {

  private static final Translations translations = GWT.create(Translations.class);

  private static final TranslationMessages translationMessages = GWT.create(TranslationMessages.class);

  @Inject
  private Provider<AttributeDialogPresenter> attributeDialogPresenterProvider;

  private VariableDto currentVariable;

  private ViewDto viewDto;

  @Inject
  public AttributesPresenter(EventBus eventBus, Display view) {
    super(eventBus, view);
  }

  public void setCurrentVariable(VariableDto currentVariable) {
    this.currentVariable = currentVariable;
  }

  public void setViewDto(ViewDto viewDto) {
    this.viewDto = viewDto;
  }

  @Override
  protected void onBind() {
    super.onBind();
    registerEventHandlers();
  }

  public void renderAttributes() {
    getView().renderAttributeRows(currentVariable.getAttributesArray());
  }

  void registerEventHandlers() {

    // show dialog
    registerHandler(getView().addAddAttributeHandler(new AddAttributeHandler()));
    getView().setEditAttributeActionHandler(new EditAttributeActionHandler());

    DeleteAttributeActionHandler deleteAttributeActionHandler = new DeleteAttributeActionHandler();
    getView().setDeleteAttributeActionHandler(deleteAttributeActionHandler);
    registerHandler(getEventBus().addHandler(ConfirmationEvent.getType(), deleteAttributeActionHandler));

    // execute attribute update
    registerHandler(getEventBus().addHandler(AttributeUpdateEvent.getType(), new AttributeUpdateEventHandler()));
  }

  private void prepareAttributeDialog(AttributeDto attributeDto) {
    AttributeDialogPresenter attributeDialogPresenter = attributeDialogPresenterProvider.get();
    attributeDialogPresenter.bind();
    attributeDialogPresenter.setViewDto(viewDto);
    attributeDialogPresenter.setAttribute(attributeDto);
    attributeDialogPresenter.setAttributes(currentVariable.getAttributesArray());
    attributeDialogPresenter.revealDisplay();
  }

  private class AddAttributeHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      prepareAttributeDialog(null);
    }
  }

  private class EditAttributeActionHandler implements ActionHandler<AttributeDto> {

    @Override
    public void doAction(AttributeDto attributeDto, String actionName) {
      if(ActionsColumn.EDIT_ACTION.equals(actionName)) {
        prepareAttributeDialog(attributeDto);
      }
    }
  }

  class DeleteAttributeActionHandler implements ActionHandler<AttributeDto>, ConfirmationEvent.Handler {

    private Runnable runDelete;

    @Override
    public void doAction(final AttributeDto deletedAttribute, String actionName) {
      if(!ActionsColumn.DELETE_ACTION.equals(actionName)) return;
      runDelete = new Runnable() {

        @Override
        public void run() {
          @SuppressWarnings("unchecked")
          JsArray<AttributeDto> deletedAttributes = (JsArray<AttributeDto>) JsArray.createArray();
          deletedAttributes.push(deletedAttribute);
          fireEvent(new AttributeUpdateEvent(deletedAttributes, UpdateType.DELETE));
        }

      };
      fireEvent(ConfirmationRequiredEvent.createWithMessages(runDelete, translations.deleteAttribute(),
          translationMessages.confirmDeleteAttribute(deletedAttribute.getName())));
    }

    @Override
    public void onConfirmation(ConfirmationEvent event) {
      if(event.getSource() == runDelete) {
        runDelete.run();
      }
    }
  }

  class AttributeUpdateEventHandler implements AttributeUpdateEvent.Handler {

    @Override
    public void onAttributeUpdate(AttributeUpdateEvent event) {
      switch(event.getUpdateType()) {
        case ADD:
          addAttribute(event);
          break;
        case EDIT:
          replaceAttribute(event);
          break;
        case DELETE:
          deleteAttributes(event.getAttributes());
          break;
      }
      getView().renderAttributeRows(currentVariable.getAttributesArray());
    }

    @SuppressWarnings("unchecked")
    private void addAttribute(AttributeUpdateEvent event) {
      if(currentVariable.getAttributesArray() == null) {
        currentVariable.setAttributesArray((JsArray<AttributeDto>) JsArray.createArray());
      }
      for(int i = 0; i < event.getAttributes().length(); i++) {
        AttributeDto newAttribute = event.getAttributes().get(i);
        currentVariable.getAttributesArray().push(newAttribute);
      }
    }

    private void replaceAttribute(AttributeUpdateEvent event) {
      deleteAttribute(event.getOriginalNamespace(), event.getOriginalName());
      for(int i = 0; i < event.getAttributes().length(); i++) {
        AttributeDto updatedAttribute = event.getAttributes().get(i);
        currentVariable.getAttributesArray().push(updatedAttribute);
      }
    }

    private void deleteAttributes(JsArray<AttributeDto> attributeDtos) {
      for(int i = 0; i < attributeDtos.length(); i++) {
        AttributeDto attributeDto = attributeDtos.get(i);
        deleteAttribute(attributeDto.getNamespace(), attributeDto.getName());
      }
    }

    private void deleteAttribute(String namespace, String name) {
      String safeNamespace = Strings.nullToEmpty(namespace);
      String safeName = Strings.nullToEmpty(name);
      @SuppressWarnings("unchecked")
      JsArray<AttributeDto> result = (JsArray<AttributeDto>) JsArray.createArray();
      for(int i = 0; i < currentVariable.getAttributesArray().length(); i++) {
        AttributeDto attribute = currentVariable.getAttributesArray().get(i);
        if(!(attribute.getNamespace().equals(safeNamespace) && attribute.getName().equals(safeName))) {
          result.push(attribute);
        }
      }
      currentVariable.setAttributesArray(result);
    }

  }

  public interface Display extends View {

    void setEditAttributeActionHandler(ActionHandler<AttributeDto> editAttributeActionHandler);

    void setDeleteAttributeActionHandler(ActionHandler<AttributeDto> deleteAttributeActionHandler);

    void renderAttributeRows(JsArray<AttributeDto> attributes);

    HandlerRegistration addAddAttributeHandler(ClickHandler addAttributeHandler);

    void formEnable(boolean enabled);
  }

}
