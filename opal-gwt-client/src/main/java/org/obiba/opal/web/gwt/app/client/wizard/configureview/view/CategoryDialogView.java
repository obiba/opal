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
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

public class CategoryDialogView extends ViewImpl implements CategoryDialogPresenter.Display {

  @UiTemplate("CategoryDialogView.ui.xml")
  interface CategoryDialogUiBinder extends UiBinder<DialogBox, CategoryDialogView> {}

  private static final CategoryDialogUiBinder uiBinder = GWT.create(CategoryDialogUiBinder.class);

  private final Widget uiWidget;

  @UiField
  DialogBox dialog;

  @UiField
  Button saveButton;

  @UiField
  Button cancelButton;

  @UiField
  TextBox categoryName;

  @UiField
  CheckBox isMissing;

  @UiField
  SimplePanel simplePanel;

  private LabelListPresenter.Display inputField;

  public CategoryDialogView() {
    uiWidget = uiBinder.createAndBindUi(this);
    uiBinder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return uiWidget;
  }

  @Override
  public void clear() {
    categoryName.setText("");
    isMissing.setValue(false);

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

  @Override
  public HasValue<Boolean> getMissing() {
    return isMissing;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public HasCloseHandlers getDialog() {
    return dialog;
  }

  @Override
  public void addInputField(LabelListPresenter.Display inputField) {
    simplePanel.clear();
    simplePanel.add(inputField.asWidget());
    this.inputField = inputField;
  }

  @Override
  public void removeInputField() {
    simplePanel.clear();
    inputField = null;
  }

  @Override
  public HasText getCaption() {
    return dialog;
  }
}
