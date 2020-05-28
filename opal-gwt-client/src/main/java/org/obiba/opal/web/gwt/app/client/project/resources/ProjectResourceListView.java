/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.resources;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Controls;
import com.github.gwtbootstrap.client.ui.TabPanel;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.support.FilterHelper;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.TextBoxClearable;
import org.obiba.opal.web.gwt.app.client.ui.celltable.*;
import org.obiba.opal.web.gwt.markdown.client.Markdown;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;
import org.obiba.opal.web.model.client.opal.ResourceFactoryDto;
import org.obiba.opal.web.model.client.opal.ResourceReferenceDto;
import org.obiba.opal.web.model.client.opal.ResourceSummaryDto;

import java.util.ArrayList;
import java.util.List;

import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.*;

public class ProjectResourceListView extends ViewWithUiHandlers<ProjectResourceListUiHandlers> implements ProjectResourceListPresenter.Display {

  private final Translations translations;

  private final TranslationMessages translationMessages;

  private final PlaceManager placeManager;

  private final ResourceProvidersService resourceProvidersService;

  interface Binder extends UiBinder<Widget, ProjectResourceListView> {
  }

  @UiField
  TabPanel resourcesTabPanel;

  @UiField
  Alert noResourceProvidersPanel;

  @UiField
  Button addResource;

  @UiField
  Button refresh;

  @UiField
  Controls filterControls;

  @UiField
  TextBoxClearable filter;

  @UiField
  OpalSimplePager pager;

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
  Table<ResourceReferenceDto> table;

  @UiField
  Panel permissionsPanel;

  private List<ResourceReferenceDto> resources;

  private final ListDataProvider<ResourceReferenceDto> dataProvider = new ListDataProvider<ResourceReferenceDto>();

  private CheckboxColumn<ResourceReferenceDto> checkColumn;

  private ActionsColumn<ResourceReferenceDto> actionsColumn;

  @Inject
  public ProjectResourceListView(Binder uiBinder, Translations translations, TranslationMessages translationMessages, PlaceManager placeManager, ResourceProvidersService resourceProvidersService) {
    this.translations = translations;
    this.resourceProvidersService = resourceProvidersService;
    this.translationMessages = translationMessages;
    this.placeManager = placeManager;
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void beforeRenderResources() {
    table.showLoadingIndicator(dataProvider);
  }

  @Override
  public void renderResources(List<ResourceReferenceDto> resources) {
    configureTable();
    table.removeColumn(checkColumn);
    table.removeColumn(actionsColumn);
    selectItemTipsAlert.setVisible(false);
    for (ResourceReferenceDto res : resources) {
      if (res.getEditable()) {
        table.insertColumn(0, checkColumn, checkColumn.getCheckColumnHeader());
        table.addColumn(actionsColumn, translations.actionsLabel());
        selectItemTipsAlert.setVisible(true);
        break;
      }
    }
    filter.setText("");
    table.hideLoadingIndicator();
    this.resources = resources;
    dataProvider.setList(resources);
    pager.firstPage();
    dataProvider.refresh();
    pager.setPagerVisible(dataProvider.getList().size() > pager.getPageSize());
    pager.setVisible(!resources.isEmpty());
    refresh.setVisible(!resources.isEmpty());
    filterControls.setVisible(!resources.isEmpty());
  }

  @Override
  public HasAuthorization getAddResourceAuthorizer() {
    return new WidgetAuthorizer(addResource);
  }

  @Override
  public HasAuthorization getPermissionsAuthorizer() {
    return new HasAuthorization() {
      @Override
      public void beforeAuthorization() {
        resourcesTabPanel.setVisible(false);
      }

      @Override
      public void authorized() {
        resourcesTabPanel.setVisible(true);
      }

      @Override
      public void unauthorized() {
        resourcesTabPanel.getWidget(0).setVisible(false);
        resourcesTabPanel.setVisible(true);
      }
    };
  }

  @Override
  public void showHasResourceProviders(boolean enabled) {
    addResource.setEnabled(enabled);
    noResourceProvidersPanel.setVisible(!enabled);
  }

  @UiHandler("addResource")
  public void onAddResource(ClickEvent event) {
    getUiHandlers().onAddResource();
  }

  @UiHandler("refresh")
  public void onRefresh(ClickEvent event) {
    getUiHandlers().onRefresh();
  }

  @UiHandler("filter")
  public void onFilterUpdate(KeyUpEvent event) {
    List<ResourceReferenceDto> filteredResources = Lists.newArrayList();
    String text = filter.getText();
    if (!Strings.isNullOrEmpty(text)) {
      List<String> tokens = FilterHelper.tokenize(text);
      for (ResourceReferenceDto resource : resources) {
        ResourceSummaryDto res = resource.getResource();
        String indexText = Joiner.on(" ").join(resource.getName(), res.getUrl(), res.hasFormat() ? res.getFormat() : "");
        if (FilterHelper.matches(indexText, tokens)) filteredResources.add(resource);
      }
    } else
      filteredResources = resources;
    dataProvider.setList(filteredResources);
    pager.firstPage();
    dataProvider.refresh();
  }

  @UiHandler("deleteResources")
  void onDeleteTables(ClickEvent event) {
    getUiHandlers().onRemoveResources(checkColumn.getSelectedItems());
    checkColumn.clearSelection();
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    if (content != null) {
      if (slot == RESOURCES_PERMISSIONS.getRawSlot()) {
        permissionsPanel.clear();
        permissionsPanel.add(content.asWidget());
      }
    }
  }

  private void configureTable() {
    if (actionsColumn != null) return;

    checkColumn = new CheckboxColumn<ResourceReferenceDto>(new ResourcesCheckDisplay());
    table.addColumn(checkColumn, checkColumn.getCheckColumnHeader());
    table.setColumnWidth(checkColumn, 1, Style.Unit.PX);

    table.addColumn(new NameColumn(new ResourceLinkCell(placeManager)), translations.nameLabel());

    table.addColumn(new TextColumn<ResourceReferenceDto>() {
      @Override
      public String getValue(ResourceReferenceDto object) {
        ResourceFactoryDto factory = resourceProvidersService.getResourceFactory(object.getProvider(), object.getFactory());
        if (factory == null || !factory.hasTitle()) return object.getProvider() + ":" + object.getFactory();
        return factory.getTitle();
      }
    }, translations.typeLabel());


    table.addColumn(new ResourceDescriptionColumn(), translations.descriptionLabel());

    table.addColumn(new ResourceURLColumn(), translations.urlLabel());

    table.addColumn(new TextColumn<ResourceReferenceDto>() {
      @Override
      public String getValue(ResourceReferenceDto object) {
        if (object.hasResource())
          return object.getResource().getFormat();
        return "";
      }
    }, translations.formatLabel());

    table.addColumn(actionsColumn = new ActionsColumn<ResourceReferenceDto>(new ActionsProvider<ResourceReferenceDto>() {

      @Override
      public String[] allActions() {
        return new String[]{VIEW_ACTION, EDIT_ACTION, REMOVE_ACTION};
      }

      @Override
      public String[] getActions(ResourceReferenceDto value) {
        if (value.hasEditable() && value.getEditable())
          return new String[]{EDIT_ACTION, REMOVE_ACTION};
        else
          return new String[]{};
      }
    }), translations.actionsLabel());

    actionsColumn.setActionHandler(new ActionHandler<ResourceReferenceDto>() {
      @Override
      public void doAction(ResourceReferenceDto object, String actionName) {
        if (actionName.equals(EDIT_ACTION))
          getUiHandlers().onEditResource(object);
        else if (actionName.equals(REMOVE_ACTION))
          getUiHandlers().onRemoveResource(object);
      }
    });

    table.setEmptyTableWidget(new Label(translations.noItems()));
    pager.setDisplay(table);
    dataProvider.addDataDisplay(table);
    renderResources(new ArrayList<ResourceReferenceDto>());
  }

  private static class ResourceLinkCell extends PlaceRequestCell<ResourceReferenceDto> {

    private ResourceLinkCell(PlaceManager placeManager) {
      super(placeManager);
    }

    @Override
    public PlaceRequest getPlaceRequest(ResourceReferenceDto resourceReferenceDto) {
      return ProjectPlacesHelper.getResourcePlace(resourceReferenceDto.getProject(), resourceReferenceDto.getName());
    }

    @Override
    public String getText(ResourceReferenceDto resourceReferenceDto) {
      return resourceReferenceDto.getName();
    }
  }

  private static final class NameColumn extends Column<ResourceReferenceDto, ResourceReferenceDto> {

    private NameColumn(ResourceLinkCell cell) {
      super(cell);
      setSortable(true);
      setDefaultSortAscending(true);
    }

    @Override
    public ResourceReferenceDto getValue(ResourceReferenceDto resourceReferenceDto) {
      return resourceReferenceDto;
    }

  }

  private class ResourcesCheckDisplay implements CheckboxColumn.Display<ResourceReferenceDto> {
    @Override
    public Table<ResourceReferenceDto> getTable() {
      return table;
    }

    @Override
    public Object getItemKey(ResourceReferenceDto item) {
      return item.getName();
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
    public void selectAllItems(CheckboxColumn.ItemSelectionHandler<ResourceReferenceDto> handler) {
      for (ResourceReferenceDto item : dataProvider.getList())
        handler.onItemSelection(item);
    }

    @Override
    public String getNItemLabel(int nb) {
      return translationMessages.nResourcesLabel(nb).toLowerCase();
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

  private class ResourceDescriptionColumn extends Column<ResourceReferenceDto, String> {

    public ResourceDescriptionColumn() {
      super(new HTMLCell());
    }

    @Override
    public String getValue(ResourceReferenceDto object) {
      return object.hasDescription() ? Markdown.parse(object.getDescription()) : "";
    }
  }

  private class ResourceURLColumn extends Column<ResourceReferenceDto, String> {

    public ResourceURLColumn() {
      super(new HTMLCell());
    }

    @Override
    public String getValue(ResourceReferenceDto object) {
      if (object.hasResource() && object.getResource().hasUrl()) {
        String url = object.getResource().getUrl();
        String urlTxt = url.length() > 50 ? url.substring(0, 50) + " ..." : url;
        if (url.startsWith("http://") || url.startsWith("https://")) {
          return "<a href='" + url + "' target='_blank' title='" + url + "'>" + urlTxt + "</a>";
        } else if (!urlTxt.equals(url)) {
          return "<span title='" + url + "'>" + urlTxt + "</span>";
        } else {
          return url;
        }
      }
      return "";
    }
  }

}
