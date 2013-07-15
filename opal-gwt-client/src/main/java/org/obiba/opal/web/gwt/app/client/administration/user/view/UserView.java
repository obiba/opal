/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.user.view;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.administration.user.presenter.UserPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.workbench.view.GroupSuggestOracle;
import org.obiba.opal.web.gwt.app.client.workbench.view.ResizeHandle;
import org.obiba.opal.web.gwt.app.client.workbench.view.SuggestListBox;
import org.obiba.opal.web.model.client.opal.GroupDto;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.PasswordTextBox;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupViewImpl;

/**
 *
 */
public class UserView extends PopupViewImpl implements UserPresenter.Display {

  @UiTemplate("UserView.ui.xml")
  interface ViewUiBinder extends UiBinder<DialogBox, UserView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private final Widget widget;

  @UiField
  DialogBox dialog;

  //  @UiField
//  DockLayoutPanel contentLayout;
//
  @UiField
  ResizeHandle resizeHandle;

  @UiField
  Button saveButton;

  @UiField
  Button cancelButton;

  @UiField(provided = true)
  SuggestListBox groups;

  @UiField
  TextBox userName;

  @UiField
  PasswordTextBox password;

  @UiField
  PasswordTextBox confirmPassword;

  private final ListDataProvider<GroupDto> groupDataProvider = new ListDataProvider<GroupDto>();

  private GroupSuggestOracle oracle;

  Column<GroupDto, GroupDto> status;

  private String originalUserName;

  private List<String> originalGroups;

  @Inject
  public UserView(EventBus eventBus) {
    super(eventBus);
    oracle = new GroupSuggestOracle(eventBus);
    groups = new SuggestListBox(oracle);

    widget = uiBinder.createAndBindUi(this);
    dialog.setText(translations.addUpdateUserLabel());

    groups.getSuggestBox().getValueBox().addKeyUpHandler(new KeyUpHandler() {

      @Override
      public void onKeyUp(KeyUpEvent event) {
        // Keycode for comma
        if(event.getNativeEvent().getKeyCode() == 188) {
          addSearchItem(groups.getSuggestBox().getText().replace(",", "").trim());
          groups.getSuggestBox().setText("");
        }
      }
    });
  }

  @Override
  public void setUser(String originalUserName, List<String> originalGroups) {
    this.originalUserName = originalUserName;
    this.originalGroups = originalGroups;
    userName.setText(originalUserName);
  }

  @Override
  public void usernameSetEnabled(boolean b) {
    userName.setEnabled(b);
  }

  @Override
  public String getUserName() {
    return userName.getText();
  }

  @Override
  public String getPassword() {
    return password.getText();
  }

  @Override
  public String getConfirmPassword() {
    return confirmPassword.getText();
  }

  @Override
  public void setPasswordError(boolean b) {
    if(b) {
      password.addStyleName("error");
      confirmPassword.addStyleName("error");
    }
  }

  @Override
  public JsArrayString getGroups() {
    JsArrayString g = JsArrayString.createArray().cast();
    for(String s : groups.getSelectedItemsTexts()) {
      g.push(s);
    }

    return g;
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public HasClickHandlers getSaveButton() {
    return saveButton;
  }

  @Override
  public HasClickHandlers getCancelButton() {
    return cancelButton;
  }

  @Override
  public void addSearchItem(String text) {
    String qText = text;
    groups.addItem(qText);
  }

  @Override
  public HandlerRegistration addSearchSelectionHandler(final SelectionHandler<SuggestOracle.Suggestion> handler) {
    return groups.getSuggestBox().addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
      @Override
      public void onSelection(SelectionEvent<SuggestOracle.Suggestion> event) {
        addSearchItem(((GroupSuggestOracle.GroupSuggestion) event.getSelectedItem()).getGroup());
        groups.getSuggestBox().setText("");
        handler.onSelection(event);
      }
    });
  }
}
