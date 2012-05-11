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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.util.AttributeDtos;
import org.obiba.opal.web.gwt.app.client.validator.AbstractFieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.LabelListPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.AttributeUpdateEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.UpdateType;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.VariableListViewDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.inject.Inject;

public class AttributeDialogPresenter extends WidgetPresenter<AttributeDialogPresenter.Display> {

  private static final Translations translations = GWT.create(Translations.class);

  private JsArray<AttributeDto> attributes;

  private final Collection<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

  private AttributeDto attributeDto;

  @Inject
  private LabelListPresenter labelListPresenter;

  private ViewDto viewDto;

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
    addValidators();
    getDisplay().showDialog();
  }

  @Override
  protected void onBind() {
    labelListPresenter.bind();
    labelListPresenter.getDisplay().setUseTextArea(true);
    getDisplay().addInputField(labelListPresenter.getDisplay());
    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
    labelListPresenter.unbind();
    getDisplay().removeInputField();

    // Reset attributeNameToDisplay to null, otherwise an Edit followed by an Add will look like another Edit.
    setAttribute(null);

    validators.clear();
  }

  protected void initDisplayComponents() {
    setTitle();
    labelListPresenter.setAttributeToDisplay(attributeDto == null ? null : attributeDto.getNamespace(),
        attributeDto == null ? null : attributeDto.getName());

    if(isEdit()) {
      labelListPresenter.setAttributes(attributes);
      labelListPresenter.updateFields();
    } else {
      getDisplay().clear();
    }
  }

  private void addValidators() {
    validators.add(new RequiredTextValidator(getDisplay().getName(), translations.attributeNameRequired()));
    validators.add(new UniqueAttributeNameValidator(translations.attributeNameAlreadyExists()));
    validators.add(labelListPresenter.new BaseLanguageTextRequiredValidator(translations.attributeValueRequired()));
  }

  private void setTitle() {
    getDisplay().getCaption().setText(isEdit() ? translations.editAttribute() : translations.addNewAttribute());
  }

  private void addEventHandlers() {
    registerHandler(getDisplay().getSaveButton().addClickHandler(new SaveClickHandler()));

    registerHandler(getDisplay().getCancelButton().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getDisplay().hideDialog();
      }
    }));

    // Hiding the Attribute dialog will generate a CloseEvent.
    registerHandler(getDisplay().getDialog().addCloseHandler(new CloseHandler<DialogBox>() {
      @Override
      public void onClose(CloseEvent<DialogBox> event) {
        unbind();
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

  private boolean isEdit() {
    return attributeDto != null;
  }

  public void setAttribute(AttributeDto attributeDto) {
    this.attributeDto = attributeDto;
    getDisplay().setAttribute(attributeDto);
  }

  public void setAttributes(JsArray<AttributeDto> attributes) {
    this.attributes = JsArrays.toSafeArray(attributes);
  }

  public void setViewDto(ViewDto viewDto) {
    this.viewDto = viewDto;
    getDisplay().setUniqueNames(findUniqueAttributeNames());
    labelListPresenter.setDatasourceName(viewDto.getDatasourceName());
  }

  private Collection<String> findUniqueAttributeNames() {
    Collection<String> uniqueNames = new TreeSet<String>();
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
    return uniqueNames;
  }

  public class UniqueAttributeNameValidator extends AbstractFieldValidator {

    public UniqueAttributeNameValidator(String errorMessageKey) {
      super(errorMessageKey);
    }

    @Override
    protected boolean hasError() {
      if(isEdit()) {
        // Edits can have the same name.
        if(getDisplay().getName().getText().equals(attributeDto.getName())) return false;
      }

      for(int i = 0; i < attributes.length(); i++) {
        AttributeDto dto = attributes.get(i);
        // Using the same name as an existing attribute is not permitted.
        if(getDisplay().getName().getText().equals(dto.getName())) return true;
      }
      return false;
    }

  }

  private class SaveClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      String errorMessageKey = validate();
      if(errorMessageKey != null) {
        eventBus.fireEvent(NotificationEvent.newBuilder().error(errorMessageKey).build());
        return;
      }
      eventBus.fireEvent(new AttributeUpdateEvent(getNewAttributeDtos(), isEdit() ? UpdateType.EDIT : UpdateType.ADD));
      getDisplay().hideDialog();
    }

    private JsArray<AttributeDto> getNewAttributeDtos() {
      @SuppressWarnings("unchecked")
      JsArray<AttributeDto> attributesArray = (JsArray<AttributeDto>) JsArray.createArray();
      Map<String, TextBoxBase> labelMap = labelListPresenter.getDisplay().getLanguageLabelMap();
      String namespace = getDisplay().getNamespace().getText();
      String name = getDisplay().getName().getText();
      for(Map.Entry<String, TextBoxBase> entry : labelMap.entrySet()) {
        String value = entry.getValue().getValue();
        if(!Strings.isNullOrEmpty(value)) {
          String locale = entry.getKey();
          attributesArray.push(AttributeDtos.create(namespace, name, value, locale));
        }
      }
      return attributesArray;
    }
  }

  public interface Display extends WidgetDisplay {

    HasCloseHandlers<DialogBox> getDialog();

    void clear();

    void showDialog();

    void hideDialog();

    HasClickHandlers getSaveButton();

    HasClickHandlers getCancelButton();

    void addInputField(LabelListPresenter.Display inputField);

    void removeInputField();

    HasText getCaption();

    HasText getNamespace();

    HasText getName();

    void setAttribute(AttributeDto attributeDto);

    void setUniqueNames(Collection<String> uniqueNames);
  }
}
