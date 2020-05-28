/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.identifiersmappings;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.core.client.JsArray;
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
import org.obiba.opal.web.gwt.app.client.permissions.view.ResourcePermissionsView;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.model.client.opal.ProjectDto.IdentifiersMappingDto;

import java.util.List;

import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.EDIT_ACTION;
import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.REMOVE_ACTION;

public class ProjectIdentifiersMappingsView extends ViewWithUiHandlers<ProjectIdentifiersMappingsUiHandlers>
  implements ProjectIdentifiersMappingsPresenter.Display {

  interface Binder extends UiBinder<Widget, ProjectIdentifiersMappingsView  > {}

  private ActionsColumn<IdentifiersMappingDto> actionColumn;

  private JsArray<IdentifiersMappingDto> idMappings;

  @UiField
  Table<IdentifiersMappingDto> mappingsTable;
  @UiField
  Button addMappingButton;

  private ListDataProvider<IdentifiersMappingDto> dataProvider = new ListDataProvider<IdentifiersMappingDto>();

  private final Translations translations;

  @Inject
  public ProjectIdentifiersMappingsView(Binder uiBinder, Translations translations) {
    initWidget(uiBinder.createAndBindUi(this));
    this.translations = translations;
    initTable();
  }

  @Override
  public HasActionHandler<IdentifiersMappingDto> getActionColumn() {
    return actionColumn;
  }

  @Override
  public void setIdentifiersMappings(List<IdentifiersMappingDto> idMappings) {
    if (idMappings != null) {
      dataProvider.setList(idMappings);
      dataProvider.refresh();
    }
  }

  @Override
  public void enableView(boolean enable) {
    addMappingButton.setEnabled(enable);
  }

  private void initTable() {
    mappingsTable.setEmptyTableWidget(new Label(translations.noIdentifiersMappings()));
    addTableColumns();
  }

  private void addTableColumns() {
    mappingsTable.addColumn(new TextColumn<IdentifiersMappingDto>() {
      @Override
      public String getValue(IdentifiersMappingDto dto) {
        return dto.getEntityType();
      }
    }, translations.entityTypeLabel());

    mappingsTable.addColumn(new TextColumn<IdentifiersMappingDto>() {
      @Override
      public String getValue(IdentifiersMappingDto dto) {
        return dto.getMapping();
      }
    }, translations.identifiersMappings());

    actionColumn = createActionColumn();
    mappingsTable.addColumn(actionColumn, translations.actionsLabel());
    dataProvider.addDataDisplay(mappingsTable);
  }

  private ActionsColumn<IdentifiersMappingDto> createActionColumn() {
    return new ActionsColumn<IdentifiersMappingDto>(new ActionsProvider<IdentifiersMappingDto>() {
      @Override
      public String[] allActions() {
        return new String[] {EDIT_ACTION, REMOVE_ACTION};
      }

      @Override
      public String[] getActions(IdentifiersMappingDto value) {
        return allActions();
      }
    });
  }

  @UiHandler("addMappingButton")
  public void addMappingClick(ClickEvent event) {
    getUiHandlers().addIdMappings();
  }

}
