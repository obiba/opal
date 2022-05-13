/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.users.profile.admin;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.common.base.Joiner;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.celltable.*;
import org.obiba.opal.web.gwt.datetime.client.Moment;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.opal.SubjectProfileDto;

import java.util.Collections;
import java.util.List;

import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.REMOVE_ACTION;

public class SubjectProfilesAdministrationView extends ViewWithUiHandlers<SubjectProfilesAdministrationUiHandlers>
    implements SubjectProfilesAdministrationPresenter.Display {

  interface Binder extends UiBinder<Widget, SubjectProfilesAdministrationView> {
  }

  @UiField
  OpalSimplePager profilesPager;

  @UiField
  Alert selectAllItemsAlert;

  @UiField
  Alert selectItemTipsAlert;

  @UiField
  Label selectAllStatus;

  @UiField
  IconAnchor selectAllAnchor;

  @UiField
  IconAnchor clearSelectionAnchor;

  @UiField
  Table<SubjectProfileDto> profilesTable;

  @UiField
  HasWidgets breadcrumbs;

  private final Translations translations;

  private final TranslationMessages translationMessages;

  private final ListDataProvider<SubjectProfileDto> profilesDataProvider = new ListDataProvider<SubjectProfileDto>();

  private CheckboxColumn<SubjectProfileDto> checkColumn;

  private ActionsColumn<SubjectProfileDto> actionsColumn;

  @Inject
  public SubjectProfilesAdministrationView(Binder uiBinder, Translations translations, TranslationMessages translationMessages) {
    this.translations = translations;
    this.translationMessages = translationMessages;
    initWidget(uiBinder.createAndBindUi(this));
    initTable();
  }

  private void initTable() {
    checkColumn = new CheckboxColumn<SubjectProfileDto>(new SubjectProfileCheckDisplay());
    profilesTable.addColumn(checkColumn, checkColumn.getCheckColumnHeader());
    profilesTable.setColumnWidth(checkColumn, 1, Style.Unit.PX);

    profilesTable.addColumn(new TextColumn<SubjectProfileDto>() {

      @Override
      public String getValue(SubjectProfileDto object) {
        return object.getPrincipal();
      }

    }, translations.userNameLabel());
    profilesTable.addColumn(new TextColumn<SubjectProfileDto>() {

      @Override
      public String getValue(SubjectProfileDto object) {
        return object.getRealm();
      }

    }, translations.realmLabel());

    profilesTable.addColumn(new TextColumn<SubjectProfileDto>() {

      @Override
      public String getValue(SubjectProfileDto object) {
        String groupsTxt = "";
        if (object.getGroupsCount() > 0)
          groupsTxt = Joiner.on(", ").join(JsArrays.toIterable(object.getGroupsArray()));
        return groupsTxt;
      }

    }, translations.userGroupsLabel());

    Column<SubjectProfileDto, Boolean> otp = new Column<SubjectProfileDto, Boolean>(new IconCell<Boolean>() {
      @Override
      public IconType getIconType(Boolean value) {
        if (value == null)
          return IconType.QUESTION;
        return value ? IconType.OK : null;
      }
    }) {
      @Override
      public Boolean getValue(SubjectProfileDto object) {
        if (object.getRealm().equals("opal-ini-realm") || object.getRealm().equals("opal-user-realm"))
          return object.getOtpEnabled();
        return null;
      }
    };
    profilesTable.addColumn(otp, "2FA");
    profilesTable.addColumn(new TextColumn<SubjectProfileDto>() {

      @Override
      public String getValue(SubjectProfileDto object) {
        return ValueRenderer.DATETIME.render(object.getCreated());
      }

    }, translations.createdLabel());
    profilesTable.addColumn(new TextColumn<SubjectProfileDto>() {

      @Override
      public String getValue(SubjectProfileDto object) {
        return Moment.create(object.getLastUpdate()).fromNow();//ValueRenderer.DATETIME.render(object.getLastUpdate());
      }

    }, translations.lastUpdatedLabel());
    profilesTable
        .addColumn(actionsColumn = new ActionsColumn<SubjectProfileDto>(new ActionsProvider<SubjectProfileDto>() {

          @Override
          public String[] allActions() {
            return new String[]{ REMOVE_ACTION, DISABLE_OTP_ACTION };
          }

          @Override
          public String[] getActions(SubjectProfileDto value) {
            if (value.getOtpEnabled()) {
              return new String[]{ REMOVE_ACTION, DISABLE_OTP_ACTION };
            }
            return new String[]{ REMOVE_ACTION };
          }
        }), translations.actionsLabel());

    profilesTable.setEmptyTableWidget(new Label(translations.noDataAvailableLabel()));
    profilesPager.setDisplay(profilesTable);
    profilesDataProvider.addDataDisplay(profilesTable);
  }

  @Override
  public void renderProfiles(List<SubjectProfileDto> rows) {
    renderRows(rows, profilesDataProvider, profilesPager);
  }

  @Override
  public HasActionHandler<SubjectProfileDto> getActions() {
    return actionsColumn;
  }

  @Override
  public void clear() {
    renderProfiles(Collections.<SubjectProfileDto>emptyList());
  }

  @UiHandler("deleteOptions")
  void onDeleteOptions(ClickEvent event) {
    getUiHandlers().onRemoveProfiles(checkColumn.getSelectedItems());
    checkColumn.clearSelection();
  }

  @UiHandler("refresh")
  public void onRefresh(ClickEvent event) {
    profilesTable.showLoadingIndicator(profilesDataProvider);
    getUiHandlers().onRefresh();
  }

  private <T> void renderRows(List<T> rows, ListDataProvider<T> dataProvider, OpalSimplePager pager) {
    dataProvider.setList(rows);
    pager.firstPage();
    dataProvider.refresh();
    pager.setPagerVisible(dataProvider.getList().size() > pager.getPageSize());
  }

  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
  }

  private class SubjectProfileCheckDisplay implements CheckboxColumn.Display<SubjectProfileDto> {


    @Override
    public Table<SubjectProfileDto> getTable() {
      return profilesTable;
    }

    @Override
    public Object getItemKey(SubjectProfileDto item) {
      return item.getPrincipal();
    }

    @Override
    public IconAnchor getClearSelection() {
      return clearSelectionAnchor;
    }

    @Override
    public IconAnchor getSelectAll() {
      return selectAllAnchor;
    }

    @Override
    public HasText getSelectAllStatus() {
      return selectAllStatus;
    }

    @Override
    public void selectAllItems(CheckboxColumn.ItemSelectionHandler<SubjectProfileDto> handler) {
      for (SubjectProfileDto item : profilesDataProvider.getList())
        handler.onItemSelection(item);
    }

    @Override
    public String getNItemLabel(int nb) {
      return translationMessages.nSubjectProfilesLabel(nb).toLowerCase();
    }

    @Override
    public Alert getSelectActionsAlert() {
      return selectAllItemsAlert;
    }

    @Override
    public Alert getSelectTipsAlert() {
      return selectItemTipsAlert;
    }
  }
}
