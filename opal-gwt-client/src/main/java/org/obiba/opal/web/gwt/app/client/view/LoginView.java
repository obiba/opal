package org.obiba.opal.web.gwt.app.client.view;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.ListDataProvider;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.LoginPresenter;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.Brand;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.HelpBlock;
import com.github.gwtbootstrap.client.ui.Image;
import com.github.gwtbootstrap.client.ui.PasswordTextBox;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.github.gwtbootstrap.client.ui.event.ClosedEvent;
import com.github.gwtbootstrap.client.ui.event.ClosedHandler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyUpHandlers;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.model.client.opal.AuthClientDto;
import org.obiba.opal.web.model.client.opal.ProjectDto;

import java.util.ArrayList;
import java.util.List;

public class LoginView extends ViewImpl implements LoginPresenter.Display {

  interface Binder extends UiBinder<Widget, LoginView> {}

  @UiField
  Panel alertPanel;

  @UiField
  TextBox userName;

  @UiField
  PasswordTextBox password;

  @UiField
  Button login;

  @UiField
  ControlGroup passwordGroup;

  @UiField
  HelpBlock passwordHelp;

  @UiField
  Brand applicationName;

  @UiField
  Image loginProgress;

  @UiField
  VerticalPanel authClientsPanel;

  //@UiField
  //Table<AuthClientDto> authClientsTable;

  //private final ListDataProvider<AuthClientDto> authClientsDataProvider = new ListDataProvider<AuthClientDto>();

  private final Translations translations;

  @Inject
  public LoginView(Binder uiBinder, Translations translations) {
    initWidget(uiBinder.createAndBindUi(this));
    this.translations = translations;
    userName.setFocus(true);
    password.addKeyPressHandler(new CapsLockTestKeyPressesHandler());
      authClientsPanel.setBorderWidth(5);
      authClientsPanel.setVisible(true);
      //authClientsPanel.setStylePrimaryName();

    //initAuthClientsTable();
  }

  @Override
  public HasClickHandlers getSignIn() {
    return login;
  }

  @Override
  public HasValue<String> getPassword() {
    return password;
  }

  @Override
  public HasValue<String> getUserName() {
    return userName;
  }

  @Override
  public void focusOnUserName() {
    userName.setFocus(true);
  }

  @Override
  public void showErrorMessageAndClearPassword() {
    clear();
    final Alert alert = new Alert(translations.authFailed(), AlertType.ERROR);
    alert.addClosedHandler(new ClosedHandler() {
      @Override
      public void onClosed(ClosedEvent closedEvent) {
        alert.removeFromParent();
      }
    });
    alertPanel.add(alert);

    Timer nonStickyTimer = new Timer() {
      @Override
      public void run() {
        alert.close();
      }
    };
    nonStickyTimer.schedule(3000);
  }

  @Override
  public void clear() {
    alertPanel.clear();
    clearPassword();
  }

  @Override
  public HasKeyUpHandlers getPasswordTextBox() {
    return password;
  }

  @Override
  public HasKeyUpHandlers getUserNameTextBox() {
    return userName;
  }

  @Override
  public void setApplicationName(String text) {
    applicationName.setText(text);
    if (Document.get() != null) {
      Document.get().setTitle (text);
    }
  }

  @Override
  public void setBusy(boolean value) {
    userName.setEnabled(!value);
    password.setEnabled(!value);
    login.setEnabled(!value);
    loginProgress.setVisible(value);
    RootPanel.get().getBodyElement().getStyle().setCursor(value ? Style.Cursor.WAIT : Style.Cursor.DEFAULT);
  }

    @Override
    public void renderAuthClients(JsArray<AuthClientDto> clients) {
        List<Widget> widgets = new ArrayList<>();
        for (int i=0;i<clients.length(); i++) {
            AuthClientDto client = clients.get(i);
            String url = client.getRedirectUrl();
            if (url == null) {
                continue; //not interested in clients without redirect url
            }

            String key = client.getName();
            String title = translations.authClientsTitleMap().get(key);
            if (title == null) {
                title = key; //fallback
            }
            Anchor anchor = new Anchor(title, false, url);
            String icon = translations.authClientsIconMap().get(key);

            if (icon != null) {
                Image img = null;
                anchor.getElement().appendChild(img.getElement());
            }
            widgets.add(anchor);
        }

        if (widgets.size() > 0) {
            for (Widget w: widgets) {
                authClientsPanel.add(w);
            }
        }
        authClientsPanel.setVisible(widgets.size() > 0);
    }

    private void clearPassword() {
    getPassword().setValue("");
  }

/*
    private void initAuthClientsTable() {

        //tablePager.setDisplay(projectsTable);
        //projectsTable.addColumn(new NameColumn(new ProjectLinkCell(placeManager)), translations.nameLabel());
        //projectsTable.addColumn(new TitleColumn() , translations.titleLabel());
        //projectsTable.addColumn(new DescriptionColumn(), translations.descriptionLabel());
        //projectsTable.addColumn(new LastUpdatedColumn(), translations.lastUpdatedLabel());
        //authClientsTable.addC

        authClientsDataProvider.addDataDisplay(authClientsTable);
        //projectsTable.getHeader(SORTABLE_COLUMN_NAME).setHeaderStyleNames("sortable-header-column");
        //projectsTable.getHeader(SORTABLE_COLUMN_LAST_UPDATED).setHeaderStyleNames("sortable-header-column");
    }
*/
    private final class CapsLockTestKeyPressesHandler implements KeyPressHandler {

    @Override
    public void onKeyPress(KeyPressEvent event) {
      int code = event.getUnicodeCharCode();
      if((!event.isShiftKeyDown() && (code >= 65 && code <= 90)) ||
          (event.isShiftKeyDown() && (code >= 97 && code <= 122))) {
        passwordGroup.setType(ControlGroupType.WARNING);
        passwordHelp.setVisible(true);
      } else {
        passwordGroup.setType(ControlGroupType.NONE);
        passwordHelp.setVisible(false);
      }
    }
  }
}
