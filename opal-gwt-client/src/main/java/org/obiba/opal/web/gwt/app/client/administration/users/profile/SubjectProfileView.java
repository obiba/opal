/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.users.profile;

import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.model.client.opal.SubjectTokenDto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.REMOVE_ACTION;

public class SubjectProfileView extends ViewWithUiHandlers<SubjectProfileUiHandlers>
    implements SubjectProfilePresenter.Display {

  interface Binder extends UiBinder<Widget, SubjectProfileView> {
  }

  @UiField
  Paragraph groupsText;

  @UiField
  Paragraph accountText;

  @UiField
  Button userAccount;

  @UiField
  Form accountForm;

  @UiField
  Panel otpPanel;

  @UiField
  Button otpSwitch;

  @UiField
  Panel qrPanel;

  @UiField
  Image qrImage;

  @UiField
  FlowPanel bookmarks;

  @UiField
  OpalSimplePager tokensPager;

  @UiField
  CellTable<SubjectTokenDto> tokensTable;

  private ActionsColumn<SubjectTokenDto> actionsColumn;

  private final ListDataProvider<SubjectTokenDto> tokensDataProvider = new ListDataProvider<SubjectTokenDto>();

  private final TranslationMessages translationMessages;

  private final Translations translations;

  @Inject
  public SubjectProfileView(Binder uiBinder, TranslationMessages translationMessages, Translations translations) {
    this.translationMessages = translationMessages;
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    initTokensTable();
  }

  @Override
  public void renderGroups(List<String> groups) {
    if (!groups.isEmpty()) {
      String gText = Joiner.on(", ").join(groups);
      groupsText.setText(translationMessages.accountGroups(gText));
    } else {
      groupsText.setText(translationMessages.accountNoGroup());
    }
  }

  @Override
  public void enableChangePassword(boolean enabled, String realm, String accountUrl) {
    accountForm.setVisible(enabled);
    userAccount.setVisible(false);
    if (enabled) {
      accountText.setText(translationMessages.accountEditable());
    } else if (Strings.isNullOrEmpty(accountUrl)) {
      accountText.setText(translationMessages.accountNotEditable(realm));
    } else {
      userAccount.setVisible(true);
      userAccount.setHref(accountUrl);
      accountText.setText(translationMessages.accountDelegated());
    }
  }

  @Override
  public void showOtpSwitch(boolean visible) {
    otpPanel.setVisible(visible);
  }

  @Override
  public void setOtpSwitchState(boolean otpEnabled) {
    otpSwitch.setText(otpEnabled ? translations.otpDisable() : translations.otpEnable());
    otpSwitch.setIcon(otpEnabled ? IconType.UNLOCK : IconType.LOCK);
    qrPanel.setVisible(false);
  }

  @Override
  public void showQrCode(String imageUri) {
    qrPanel.setVisible(true);
    qrImage.setUrl(imageUri);
  }

  @Override
  public void renderTokens(List<SubjectTokenDto> tokens) {
    Collections.sort(tokens, new Comparator<SubjectTokenDto>() {
      @Override
      public int compare(SubjectTokenDto s1, SubjectTokenDto s2) {
        return s1.getName().compareTo(s2.getName());
      }
    });
    tokensDataProvider.setList(tokens);
    tokensPager.firstPage();
    tokensDataProvider.refresh();
    tokensPager.setPagerVisible(tokensDataProvider.getList().size() > tokensPager.getPageSize());
    tokensTable.setVisible(!tokens.isEmpty());
    tokensPager.setVisible(!tokens.isEmpty());
  }

  @UiHandler("changePassword")
  public void onChangePassword(ClickEvent event) {
    getUiHandlers().onChangePassword();
  }

  @UiHandler("otpSwitch")
  public void onSwitchOtp(ClickEvent event) {
    getUiHandlers().onOtpSwitch();
  }

  @UiHandler("addToken")
  public void onAddToken(ClickEvent event) {
    getUiHandlers().onAddToken();
  }

  @UiHandler("addDSToken")
  public void onAddDSToken(ClickEvent event) {
    getUiHandlers().onAddDataSHIELDToken();
  }

  @UiHandler("addRToken")
  public void onAddRToken(ClickEvent event) {
    getUiHandlers().onAddRToken();
  }

  @UiHandler("addSQLToken")
  public void onAddSQLToken(ClickEvent event) {
    getUiHandlers().onAddSQLToken();
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    if (slot == SubjectProfilePresenter.BOOKMARKS) {
      bookmarks.clear();
      bookmarks.add(content);
    }
  }

  private void initTokensTable() {
    tokensTable.addColumn(new TextColumn<SubjectTokenDto>() {

      @Override
      public String getValue(SubjectTokenDto object) {
        return object.getName();
      }

    }, translations.nameLabel());
    tokensTable.addColumn(new TextColumn<SubjectTokenDto>() {

      @Override
      public String getValue(SubjectTokenDto object) {
        String projects = "";
        if (object.getProjectsCount() > 0) {
          projects = Joiner.on(", ").join(JsArrays.toList(object.getProjectsArray()));
        } else {
          projects = "[" + translations.allProjectsLabel().toLowerCase() + "]";
        }
        return projects;
      }

    }, translations.pageProjectsTitle());
    tokensTable.addColumn(new TextColumn<SubjectTokenDto>() {

      @Override
      public String getValue(SubjectTokenDto object) {
        return object.getAccess() == null ? "" :
            translations.tokenAccessMap().get(object.getAccess().getName());
      }

    }, translations.dataAccessLabel());
    tokensTable.addColumn(new TextColumn<SubjectTokenDto>() {

      @Override
      public String getValue(SubjectTokenDto object) {
        String tasks = "";
        if (object.getCommandsCount() > 0) {
          for (String cmd : JsArrays.toList(object.getCommandsArray())) {
            String cmdLbl = translations.tokenTasksMap().get(cmd);
            tasks = tasks.isEmpty() ? cmdLbl : tasks + ", " + cmdLbl;
          }
        }
        return tasks;
      }

    }, translations.tasksLabel());
    tokensTable.addColumn(new TextColumn<SubjectTokenDto>() {
      @Override
      public String getValue(SubjectTokenDto object) {
        String admin = "";
        if (object.getCreateProject()) admin = "Create";
        if (object.getUpdateProject()) admin = admin.isEmpty() ? "Update" : admin + ", " + "Update";
        if (object.getDeleteProject()) admin = admin.isEmpty() ? "Delete" : admin + ", " + "Delete";
        return admin;
      }
    }, translations.administrationLabel());
    tokensTable.addColumn(new TextColumn<SubjectTokenDto>() {
      @Override
      public String getValue(SubjectTokenDto object) {
        String services = "";
        if (object.getUseR()) services = "R";
        if (object.getUseDatashield()) services = services.isEmpty() ? "DataSHIELD" : services + ", " + "DataSHIELD";
        if (object.getUseSQL()) services = services.isEmpty() ? "SQL" : services + ", " + "SQL";
        if (object.getSysAdmin()) {
          String sysAdmin = translations.pageAdministrationTitle();
          services = services.isEmpty() ? sysAdmin : services + ", " + sysAdmin;
        }
        return services;
      }
    }, translations.servicesLabel());
    tokensTable.addColumn(actionsColumn = new ActionsColumn<SubjectTokenDto>(new ActionsProvider<SubjectTokenDto>() {

      @Override
      public String[] allActions() {
        return new String[]{REMOVE_ACTION};
      }

      @Override
      public String[] getActions(SubjectTokenDto value) {
        return new String[]{REMOVE_ACTION};
      }
    }), translations.actionsLabel());

    actionsColumn.setActionHandler(new ActionHandler<SubjectTokenDto>() {
      @Override
      public void doAction(SubjectTokenDto object, String actionName) {
        getUiHandlers().onRemoveToken(object);
      }
    });

    tokensTable.setEmptyTableWidget(new Label(translations.noTokensLabel()));
    tokensPager.setDisplay(tokensTable);
    tokensDataProvider.addDataDisplay(tokensTable);
    renderTokens(new ArrayList<SubjectTokenDto>());
  }

}
