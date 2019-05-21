/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.idproviders.list;

import com.github.gwtbootstrap.client.ui.CellTable;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.celltable.*;
import org.obiba.opal.web.model.client.opal.IDProviderDto;

import java.util.Collections;
import java.util.List;

import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.EDIT_ACTION;
import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.REMOVE_ACTION;

public class IDProvidersAdministrationView extends ViewWithUiHandlers<IDProvidersAdministrationUiHandlers>
    implements IDProvidersAdministrationPresenter.Display {

  interface Binder extends UiBinder<Widget, IDProvidersAdministrationView> {
  }

  @UiField
  OpalSimplePager pager;

  @UiField
  CellTable<IDProviderDto> table;

  @UiField
  HasWidgets breadcrumbs;

  private final static Translations translations = GWT.create(Translations.class);

  private final ListDataProvider<IDProviderDto> providersDataProvider = new ListDataProvider<IDProviderDto>();

  private ActionsColumn<IDProviderDto> actionsColumn;

  @Inject
  public IDProvidersAdministrationView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
    configTable();
  }

  private void configTable() {
    table.addColumn(new TextColumn<IDProviderDto>() {

      @Override
      public String getValue(IDProviderDto object) {
        return object.getName();
      }

    }, translations.nameLabel());
    table.addColumn(new TextColumn<IDProviderDto>() {

      @Override
      public String getValue(IDProviderDto object) {
        return object.getLabel();
      }

    }, translations.labelLabel());
    table.addColumn(new TextColumn<IDProviderDto>() {

      @Override
      public String getValue(IDProviderDto object) {
        return "[" + object.getName() + "] " + object.getGroups();
      }

    }, translations.userGroupsLabel());
    table.addColumn(new AccountLoginLinkColumn(), translations.accountLoginLabel());
    table.addColumn(new DiscoveryLinkColumn(), translations.parametersLabel());
    table.addColumn(new Column<IDProviderDto, Boolean>(
        new IconCell<Boolean>() {
          @Override
          public IconType getIconType(Boolean value) {
            return value ? IconType.OK : IconType.REMOVE;
          }
        }) {
      @Override
      public Boolean getValue(IDProviderDto dto) {
        return dto.getEnabled();
      }
    }, translations.enabledLabel());
    table
        .addColumn(actionsColumn = new ActionsColumn<IDProviderDto>(new ActionsProvider<IDProviderDto>() {

          @Override
          public String[] allActions() {
            return new String[]{REMOVE_ACTION, EDIT_ACTION, ENABLE_ACTION, DISABLE_ACTION, DUPLICATE_ACTION};
          }

          @Override
          public String[] getActions(IDProviderDto value) {
            if (value.getEnabled())
              return new String[]{REMOVE_ACTION, EDIT_ACTION, DISABLE_ACTION, DUPLICATE_ACTION};
            else
              return new String[]{REMOVE_ACTION, EDIT_ACTION, ENABLE_ACTION, DUPLICATE_ACTION};
          }
        }), translations.actionsLabel());

    table.setEmptyTableWidget(new Label(translations.noDataAvailableLabel()));
    pager.setDisplay(table);
    providersDataProvider.addDataDisplay(table);
  }

  @Override
  public void renderIDProviders(List<IDProviderDto> rows) {
    renderRows(rows, providersDataProvider, pager);
  }

  @UiHandler("addProvider")
  public void onAddProvider(ClickEvent event) {
    getUiHandlers().onAddProvider();
  }


  @Override
  public HasActionHandler<IDProviderDto> getActions() {
    return actionsColumn;
  }

  @Override
  public void clear() {
    renderIDProviders(Collections.<IDProviderDto>emptyList());
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

  private static abstract class ButtonLinkCell extends LinkCell<IDProviderDto> {
    @Override
    public String getTarget(IDProviderDto value) {
      return "_blank";
    }

    @Override
    public String getLinkClass(IDProviderDto value) {
      return "btn btn-small";
    }

    @Override
    public boolean displayTextWhenNoLink(IDProviderDto value) {
      return false;
    }
  }

  private static class AccountLoginLinkColumn extends Column<IDProviderDto, IDProviderDto> {
    public AccountLoginLinkColumn() {
      super(new ButtonLinkCell() {
        @Override
        public String getLink(IDProviderDto value) {
          return value.getProviderUrl();
        }

        @Override
        public String getText(IDProviderDto value) {
          return translations.providerLoginLabel();
        }
      });
    }

    @Override
    public IDProviderDto getValue(IDProviderDto object) {
      return object;
    }
  }

  private static class DiscoveryLinkColumn extends Column<IDProviderDto, IDProviderDto> {
    public DiscoveryLinkColumn() {
      super(new ButtonLinkCell() {
        @Override
        public String getLink(IDProviderDto value) {
          return value.getDiscoveryURI();
        }

        @Override
        public String getText(IDProviderDto value) {
          return translations.discoveryURILabel();
        }
      });
    }

    @Override
    public IDProviderDto getValue(IDProviderDto object) {
      return object;
    }
  }
}
