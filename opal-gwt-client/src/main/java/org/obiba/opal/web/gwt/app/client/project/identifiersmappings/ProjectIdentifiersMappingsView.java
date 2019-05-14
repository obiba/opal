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
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.model.client.opal.ProjectDto.IdentifiersMappingDto;

import java.util.List;

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
    dataProvider.addDataDisplay(mappingsTable);
  }

  private ActionsColumn<IdentifiersMappingDto> createActionColumn() {
    return new ActionsColumn<IdentifiersMappingDto>(new ActionsProvider<IdentifiersMappingDto>() {
      @Override
      public String[] allActions() {
        return new String[] {ADD_MAPPING, EDIT_MAPPING, DELETE_MAPPING};
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
