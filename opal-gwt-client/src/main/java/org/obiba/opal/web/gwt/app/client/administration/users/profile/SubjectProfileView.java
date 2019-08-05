/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.users.profile;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CellTable;
import com.github.gwtbootstrap.client.ui.Form;
import com.github.gwtbootstrap.client.ui.Paragraph;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
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
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.model.client.opal.SubjectTokenDto;

import java.util.ArrayList;
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
  FlowPanel bookmarks;

  @UiField
  Button addToken;

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
    configTokensTable();
  }

  @Override
  public void renderGroups(List<String> groups) {
    groupsText.setText("");
    groupsText.setVisible(!groups.isEmpty());

    if (!groups.isEmpty()) {
      String gText = Joiner.on(", ").join(groups);
      groupsText.setText(translationMessages.accountGroups(gText));
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
  public void renderTokens(List<SubjectTokenDto> tokens) {
    renderRows(tokens, tokensDataProvider, tokensPager);
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

  @UiHandler("addToken")
  public void onAddToken(ClickEvent event) {
    getUiHandlers().onAddToken();
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    if (slot == SubjectProfilePresenter.BOOKMARKS) {
      bookmarks.clear();
      bookmarks.add(content);
    }
  }

  private void configTokensTable() {
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
        if (object.getProjectsCount()>0) {
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
        String tasks = "";
        if (object.getCommandsCount()>0) {
          for (String cmd : JsArrays.toList(object.getCommandsArray())) {
            String cmdLbl = translations.tokenTasksMap().get(cmd);
            tasks = tasks.isEmpty() ? cmdLbl : tasks + ", " + cmdLbl;
          }
        }
        return tasks;
      }

    }, translations.tasks());
    tokensTable.addColumn(new TextColumn<SubjectTokenDto>() {
      @Override
      public String getValue(SubjectTokenDto object) {
        String services = "";
        if (object.getUseR()) services = "R";
        if (object.getUseDatashield()) services = services.isEmpty() ? "DataSHIELD" : services + ", " + "DataSHIELD";
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

  private <T> void renderRows(List<T> rows, ListDataProvider<T> dataProvider, OpalSimplePager pager) {

  }
}
