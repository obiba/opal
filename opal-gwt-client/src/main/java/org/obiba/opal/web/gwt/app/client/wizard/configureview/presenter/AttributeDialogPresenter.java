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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;
import org.obiba.opal.web.gwt.app.client.validator.AbstractFieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.LabelListPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.AttributeUpdateEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.UpdateType;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.TextBox;
import com.google.inject.Inject;

public class AttributeDialogPresenter extends WidgetPresenter<AttributeDialogPresenter.Display> {

  public interface Display extends WidgetDisplay {

    HasCloseHandlers<DialogBox> getDialog();

    void clear();

    void showDialog();

    void hideDialog();

    HasClickHandlers getSaveButton();

    HasClickHandlers getCancelButton();

    HandlerRegistration addPredefinedAttributeNameRadioButtonClickHandler(ClickHandler handler);

    HandlerRegistration addCustomAttributeNameRadioButtonClickHandler(ClickHandler handler);

    void setAttributeNameEditable(boolean editable);

    void setLabelsEnabled(boolean enabled);

    void setCustomAttributeNameEnabled(boolean enabled);

    void selectPredefinedAttributeNameRadioButton();

    HasText getCustomAttributeName();

    void addInputField(LabelListPresenter.Display inputField);

    void removeInputField();

    HasText getAttributeName();

    void setAttributeName(String attributeName);

    void setNameDropdownList(List<String> labels);

    HasText getCaption();

  }

  private JsArray<AttributeDto> attributes;

  private String attributeNameToDisplay;

  private Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

  private Translations translations = GWT.create(Translations.class);

  @Inject
  private LabelListPresenter labelListPresenter;

  @SuppressWarnings("unchecked")
  @Inject
  public AttributeDialogPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);

    attributes = (JsArray<AttributeDto>) JsArray.createArray();
  }

  @Override
  public void refreshDisplay() {
    labelListPresenter.refreshDisplay();
  }

  @Override
  public void revealDisplay() {
    initDisplayComponents();
    getDisplay().showDialog();
  }

  @Override
  protected void onBind() {
    labelListPresenter.bind();
    getDisplay().addInputField(labelListPresenter.getDisplay());
    addEventHandlers();

    validators.add(new RequiredTextValidator(getDisplay().getAttributeName(), "AttributeNameRequired"));
    validators.add(new UniqueAttributeNameValidator("AttributeNameAlreadyExists"));
    validators.add(labelListPresenter.new BaseLanguageTextRequiredValidator("BaseLanguageLabelRequired"));
  }

  @Override
  protected void onUnbind() {
    labelListPresenter.unbind();
    getDisplay().removeInputField();

    // Reset attributeNameToDisplay to null, otherwise an Edit followed by an Add will look like another Edit.
    setAttributeNameToDisplay(null);

    validators.clear();
  }

  protected void initDisplayComponents() {
    setTitle();
    resetForm();
    labelListPresenter.setAttributeToDisplay(attributeNameToDisplay);

    if(isEdit()) {
      getDisplay().setAttributeNameEditable(false); // don't allow edits of attribute name
      getDisplay().setAttributeName(attributeNameToDisplay);
      labelListPresenter.setAttributes(attributes);
      labelListPresenter.updateFields();
    } else {
      getDisplay().setAttributeNameEditable(true);
      getDisplay().clear();
    }
  }

  private void setTitle() {
    if(isEdit()) {
      getDisplay().getCaption().setText(translations.editAttribute());
    } else {
      getDisplay().getCaption().setText(translations.addNewAttribute());
    }
  }

  private void resetForm() {
    getDisplay().selectPredefinedAttributeNameRadioButton();
    getDisplay().setLabelsEnabled(true);
    getDisplay().setCustomAttributeNameEnabled(false);
    getDisplay().getCustomAttributeName().setText("");
  }

  private void addEventHandlers() {
    super.registerHandler(getDisplay().getSaveButton().addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        String errorMessageKey = validate();
        if(errorMessageKey != null) {
          eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, errorMessageKey, null));
          return;
        }
        JsArray<AttributeDto> newAttributes = getNewAttributeDtos();
        if(isEdit()) {
          eventBus.fireEvent(new AttributeUpdateEvent(newAttributes, UpdateType.EDIT));
        } else {
          eventBus.fireEvent(new AttributeUpdateEvent(newAttributes, UpdateType.ADD));
        }
        getDisplay().hideDialog();
      }
    }));

    super.registerHandler(getDisplay().getCancelButton().addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        getDisplay().hideDialog();
      }
    }));

    // Hiding the Attribute dialog will generate a CloseEvent.
    super.registerHandler(getDisplay().getDialog().addCloseHandler(new CloseHandler<DialogBox>() {
      @Override
      public void onClose(CloseEvent<DialogBox> event) {
        unbind();
      }
    }));

    addRadioButtonNameEventHandlers();
  }

  private void addRadioButtonNameEventHandlers() {
    super.registerHandler(getDisplay().addPredefinedAttributeNameRadioButtonClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        getDisplay().setCustomAttributeNameEnabled(false);
        getDisplay().setLabelsEnabled(true);
      }
    }));

    super.registerHandler(getDisplay().addCustomAttributeNameRadioButtonClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        getDisplay().setCustomAttributeNameEnabled(true);
        getDisplay().setLabelsEnabled(false);
      }
    }));

  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  String validate() {
    for(FieldValidator validator : validators) {
      String errorMessageKey = validator.validate();
      if(errorMessageKey != null) {
        return errorMessageKey;
      }
    }
    return null;
  }

  public class UniqueAttributeNameValidator extends AbstractFieldValidator {

    public UniqueAttributeNameValidator(String errorMessageKey) {
      super(errorMessageKey);
    }

    @Override
    protected boolean hasError() {
      if(isEdit()) {
        // Edits can have the same name.
        if(getDisplay().getAttributeName().getText().equals(attributeNameToDisplay)) return false;
      }

      for(int i = 0; i < attributes.length(); i++) {
        AttributeDto dto = attributes.get(i);
        // Using the same name as an existing attribute is not permitted.
        if(getDisplay().getAttributeName().getText().equals(dto.getName())) return true;
      }
      return false;
    }

  }

  private boolean isEdit() {
    return attributeNameToDisplay != null;
  }

  private JsArray<AttributeDto> getNewAttributeDtos() {
    @SuppressWarnings("unchecked")
    JsArray<AttributeDto> attributes = (JsArray<AttributeDto>) JsArray.createArray();
    Map<String, TextBox> labelMap = labelListPresenter.getDisplay().getLanguageLabelMap();
    for(Map.Entry<String, TextBox> entry : labelMap.entrySet()) {
      if(entry.getValue().getValue() != null && !entry.getValue().getValue().equals("")) {
        AttributeDto attribute = AttributeDto.create();
        if(entry.getKey() == null || entry.getKey().equals("")) {
          attribute.clearLocale();
        } else {
          attribute.setLocale(entry.getKey());
        }
        attribute.setName(getDisplay().getAttributeName().getText());
        attribute.setValue(entry.getValue().getValue());
        attributes.push(attribute);
      }
    }
    return attributes;
  }

  public void setAttributeNameToDisplay(String attributeNameToDisplay) {
    this.attributeNameToDisplay = attributeNameToDisplay;
  }

  @SuppressWarnings("unchecked")
  public void setAttributes(JsArray<AttributeDto> attributes) {
    this.attributes = (JsArray<AttributeDto>) JsArray.createArray();

    if(attributes != null) {
      for(int attributeIndex = 0; attributeIndex < attributes.length(); attributeIndex++) {
        this.attributes.push(attributes.get(attributeIndex));
      }
    }
  }

  public void setViewDto(ViewDto viewDto) {
    labelListPresenter.setDatasourceName(viewDto.getDatasourceName());
  }
}
