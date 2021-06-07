/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.datashield.profiles.config;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CellTable;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;
import org.obiba.opal.web.model.client.datashield.DataShieldROptionDto;

import java.util.List;

import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.EDIT_ACTION;
import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.REMOVE_ACTION;

public class DataShieldROptionsView extends ViewWithUiHandlers<DataShieldROptionsUiHandlers>
    implements DataShieldROptionsPresenter.Display {

  interface Binder extends UiBinder<Widget, DataShieldROptionsView> {
  }

  @UiField
  CellTable<DataShieldROptionDto> table;

  @UiField
  OpalSimplePager pager;

  @UiField
  Button addOptionButton;

  private final ListDataProvider<DataShieldROptionDto> dataProvider = new ListDataProvider<>();

  private ActionsColumn<DataShieldROptionDto> actionsColumn;

  @Inject
  public DataShieldROptionsView(Binder uiBinder, Translations translations) {
    initWidget(uiBinder.createAndBindUi(this));
    initTable(translations);
  }

  @Override
  public void initialize(List<DataShieldROptionDto> options) {
    populateTable(options);
  }

  @Override
  public HasAuthorization addROptionsAuthorizer() {
    return new WidgetAuthorizer(addOptionButton);
  }

  @UiHandler("addOptionButton")
  public void onAddButtonClicked(ClickEvent event) {
    getUiHandlers().addOption();
  }

  private void populateTable(List<DataShieldROptionDto> options) {
    dataProvider.setList(options);
    pager.firstPage();
    dataProvider.refresh();
    pager.setPagerVisible(dataProvider.getList().size() > pager.getPageSize());
  }

  private void initTable(Translations translations) {
    table.setVisibleRange(0, 10);
    table.addColumn(new TextColumn<DataShieldROptionDto>() {

      @Override
      public String getValue(DataShieldROptionDto optionDto) {
        return optionDto.getName();
      }
    }, translations.nameLabel());
    table.addColumn(new TextColumn<DataShieldROptionDto>() {

      @Override
      public String getValue(DataShieldROptionDto optionDto) {
        return optionDto.getValue();
      }
    }, translations.valueLabel());
    table.addColumn(actionsColumn = new ActionsColumn<>(
        new ActionsProvider<DataShieldROptionDto>() {

          @Override
          public String[] allActions() {
            return new String[]{EDIT_ACTION, REMOVE_ACTION};
          }

          @Override
          public String[] getActions(DataShieldROptionDto value) {
            return allActions();
          }

        }), translations.actionsLabel());
    table.setEmptyTableWidget(new Label(translations.noOptionsLabel()));

    dataProvider.addDataDisplay(table);
    pager.setDisplay(table);
  }

  @Override
  public void setOptionActionHandler(ActionHandler<DataShieldROptionDto> handler) {
    actionsColumn.setActionHandler(handler);
  }

}