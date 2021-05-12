package org.obiba.opal.web.gwt.app.client.fs.view;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.PasswordTextBox;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.UnzipModalPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.UnzipModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePanel;

public class UnzipModalView extends ModalPopupViewWithUiHandlers<UnzipModalUiHandlers> implements UnzipModalPresenter.Display {

  interface Binder extends UiBinder<Widget, UnzipModalView> {}

  @UiField
  Modal dialog;

  @UiField
  OpalSimplePanel filePanel;

  @UiField
  PasswordTextBox password;

  @UiField
  Button cancelButton;

  @UiField
  Button viewPasswordButton;

  @Inject
  protected UnzipModalView(EventBus eventBus, Binder binder, Translations translations) {
    super(eventBus);
    initWidget(binder.createAndBindUi(this));

    dialog.setTitle(translations.unzipModalTitle());

    password.setEnabled(true);
    password.setStyleName("password-vertical-align");
    viewPasswordButton.setEnabled(true);
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public HasText getPassword() {
    return password;
  }

  @Override
  public void setFileSelectorWidgetDisplay(FileSelectionPresenter.Display display) {
    filePanel.setWidget(display.asWidget());
    display.setFieldWidth("20em");
  }

  @UiHandler("unzip")
  public void onUnzip(ClickEvent event) {
    getUiHandlers().onUnzip();
  }

  @UiHandler("cancelButton")
  public void onCancelButton(ClickEvent event) {
    dialog.hide();
  }

  @UiHandler("viewPasswordButton")
  public void onViewPassword(ClickEvent event) {
    password.getElement().setAttribute("type", password.getElement().getAttribute("type").equalsIgnoreCase("text") ? "password" : "text");
  }
}
