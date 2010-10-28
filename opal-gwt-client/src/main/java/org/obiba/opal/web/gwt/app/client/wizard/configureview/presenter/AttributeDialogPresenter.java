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
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;
import org.obiba.opal.web.gwt.app.client.validator.AbstractFieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.LabelListPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.AttributeUpdateEvent;
import org.obiba.opal.web.model.client.magma.AttributeDto;

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
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 *
 */
public class AttributeDialogPresenter extends WidgetPresenter<AttributeDialogPresenter.Display> {

  public interface Display extends WidgetDisplay {

    HasCloseHandlers<DialogBox> getDialog();

    void showDialog();

    void hideDialog();

    HasClickHandlers getSaveButton();

    HasClickHandlers getCancelButton();

    HandlerRegistration addNameDropdownRadioChoiceHandler(ClickHandler handler);

    HandlerRegistration addNameFieldRadioChoiceHandler(ClickHandler handler);

    void setLabelsEnabled(boolean enabled);

    void setAttributeNameEnabled(boolean enabled);

    void selectNameDropdownRadioChoice();

    HasText getAttributeNameField();

    void addLabelListPresenter(Widget widget);

    void removeLabelListPresenter(Widget widget);

    HasText getAttributeName();

    void setNameDropdownList(List<String> labels);

  }

  private String datasourceName;

  private JsArray<AttributeDto> attributes;

  private String attributeNameToDisplay;

  private List<String> labels;

  private Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

  @Inject
  private LabelListPresenter labelListPresenter;

  @Inject
  public AttributeDialogPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
    validators.add(new RequiredTextValidator(getDisplay().getAttributeName(), "AttributeNameRequired"));
    validators.add(new UniqueAttributeNameValidator("AttributeNameAlreadyExists"));
  }

  @Override
  public void refreshDisplay() {
    labelListPresenter.refreshDisplay();
  }

  @Override
  public void revealDisplay() {
    getDisplay().showDialog();
    labelListPresenter.revealDisplay();
  }

  @Override
  protected void onBind() {
    labelListPresenter.setAttributes(attributes);
    labelListPresenter.setAttributeToDisplay(attributeNameToDisplay);
    labelListPresenter.setDatasourceName(datasourceName);
    labelListPresenter.bind();
    validators.add(labelListPresenter.new BaseLanguageTextRequiredValidator("BaseLanguageLabelRequired"));
    getDisplay().addLabelListPresenter(labelListPresenter.getDisplay().asWidget());
    addEventHandlers();
    addRadioButtonNameEventHandlers();
    getDisplay().setNameDropdownList(labels);
    resetForm();
  }

  private void resetForm() {
    getDisplay().setLabelsEnabled(true);
    getDisplay().setAttributeNameEnabled(false);
    getDisplay().selectNameDropdownRadioChoice();
    getDisplay().getAttributeNameField().setText("");
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
          eventBus.fireEvent(new AttributeUpdateEvent(newAttributes, AttributeUpdateEvent.UpdateType.EDIT));
        } else {
          eventBus.fireEvent(new AttributeUpdateEvent(newAttributes, AttributeUpdateEvent.UpdateType.ADD));
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

  }

  private void addRadioButtonNameEventHandlers() {
    super.registerHandler(getDisplay().addNameDropdownRadioChoiceHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        getDisplay().setAttributeNameEnabled(false);
        getDisplay().setLabelsEnabled(true);
      }
    }));

    super.registerHandler(getDisplay().addNameFieldRadioChoiceHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        getDisplay().setAttributeNameEnabled(true);
        getDisplay().setLabelsEnabled(false);
      }
    }));

  }

  @Override
  protected void onUnbind() {
    getDisplay().removeLabelListPresenter(labelListPresenter.getDisplay().asWidget());
    labelListPresenter.unbind();
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
      if(getDisplay().getAttributeName().equals(attributeNameToDisplay)) return false; // Edits can have the same name.
      for(int i = 0; i < attributes.length(); i++) {
        AttributeDto dto = attributes.get(i);
        // Using the same name as an existing attribute is not permitted.
        if(getDisplay().getAttributeName().equals(dto.getName())) return true;
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
      AttributeDto attribute = AttributeDto.create();
      attribute.setLocale(entry.getKey());
      attribute.setName(getDisplay().getAttributeName().getText());
      attribute.setValue(entry.getValue().getValue());
      attributes.push(attribute);
    }
    return attributes;
  }

  public void setDatasourceName(String datasourceName) {
    this.datasourceName = datasourceName;
  }

  public void setLabels(List<String> labels) {
    this.labels = labels;
  }
}
