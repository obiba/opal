/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.configureview.view;

import java.util.Set;
import java.util.TreeSet;

import org.obiba.opal.web.gwt.app.client.widgets.presenter.LabelListPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.AttributeDialogPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.DropdownSuggestBox;
import org.obiba.opal.web.model.client.magma.AttributeDto;

import com.google.common.collect.Multimap;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class AttributeDialogView extends Composite implements AttributeDialogPresenter.Display {

  @UiTemplate("AttributeDialogView.ui.xml")
  interface MyUiBinder extends UiBinder<DialogBox, AttributeDialogView> {

  }

  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  DialogBox dialog;

  @UiField
  DropdownSuggestBox namespaceBox;

  @UiField
  DropdownSuggestBox nameBox;

  private Multimap<String, String> uniqueNames;

  @UiField
  SimplePanel simplePanel;

  private LabelListPresenter.Display inputField;

  @UiField
  Button saveButton;

  @UiField
  Button cancelButton;

  public AttributeDialogView() {
    initWidget(uiBinder.createAndBindUi(this));
    uiBinder.createAndBindUi(this);
    registerHandlers();
  }

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void clear() {
    namespaceBox.clear();
    nameBox.clear();
    if(inputField != null) inputField.clearAttributes();
  }

  @Override
  public void showDialog() {
    dialog.center();
    dialog.show();
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public Button getCancelButton() {
    return cancelButton;
  }

  @Override
  public Button getSaveButton() {
    return saveButton;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public HasCloseHandlers getDialog() {
    return dialog;
  }

  @Override
  public void addInputField(LabelListPresenter.Display inputFieldDisplay) {
    simplePanel.clear();
    simplePanel.add(inputFieldDisplay.asWidget());
    inputField = inputFieldDisplay;
  }

  @Override
  public void removeInputField() {
    simplePanel.clear();
  }

  private void initNamespaces() {
    Set<String> namespaces = new TreeSet<String>(uniqueNames.keySet());
    namespaces.remove("");
    namespaceBox.getSuggestOracle().clear();
    namespaceBox.getSuggestOracle().addAll(namespaces);
  }

  private void registerHandlers() {
    namespaceBox.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> stringValueChangeEvent) {
        String value = stringValueChangeEvent.getValue();
        nameBox.getSuggestOracle().clear();
        if(uniqueNames.containsKey(value)) {
          nameBox.getSuggestOracle().addAll(uniqueNames.get(value));
        }
      }
    });
  }

  @Override
  public HasText getCaption() {
    return dialog;
  }

  @Override
  public HasText getNamespace() {
    return namespaceBox;
  }

  @Override
  public HasText getName() {
    return nameBox;
  }

  @Override
  public void setAttribute(AttributeDto attributeDto) {
    namespaceBox.setValue(attributeDto == null ? "" : attributeDto.getNamespace());
    nameBox.setValue(attributeDto == null ? "" : attributeDto.getName());
  }

  @Override
  public void setUniqueNames(Multimap<String, String> uniqueNames) {
    this.uniqueNames = uniqueNames;
    initNamespaces();
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }
}
