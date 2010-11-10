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

import org.obiba.opal.web.gwt.app.client.widgets.presenter.LabelListPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.CategoryDialogPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class CategoryDialogView extends Composite implements CategoryDialogPresenter.Display {

  @UiTemplate("CategoryDialogView.ui.xml")
  interface CategoryDialogUiBinder extends UiBinder<DialogBox, CategoryDialogView> {
  }

  private static CategoryDialogUiBinder uiBinder = GWT.create(CategoryDialogUiBinder.class);

  @UiField
  DialogBox dialog;

  @UiField
  Button saveButton;

  @UiField
  Button cancelButton;

  @UiField
  TextBox categoryName;

  @UiField
  ScrollPanel inputFieldPanel;

  private LabelListPresenter.Display inputField;

  public CategoryDialogView() {
    initWidget(uiBinder.createAndBindUi(this));
    uiBinder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

  @Override
  public void clear() {
    categoryName.setText("");

    if(inputField != null) {
      inputField.clearAttributes();
    }
  }

  @Override
  public void showDialog() {
    dialog.center();
    dialog.show();
    categoryName.setFocus(true);
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

  @Override
  public HasText getCategoryName() {
    return categoryName;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public HasCloseHandlers getDialog() {
    return dialog;
  }

  @Override
  public void addInputField(LabelListPresenter.Display inputField) {
    inputFieldPanel.clear();
    inputFieldPanel.add(inputField.asWidget());
    this.inputField = inputField;
  }

  @Override
  public void removeInputField() {
    inputFieldPanel.clear();
    inputField = null;
  }

  @Override
  public HasText getCaption() {
    return dialog;
  }
}
