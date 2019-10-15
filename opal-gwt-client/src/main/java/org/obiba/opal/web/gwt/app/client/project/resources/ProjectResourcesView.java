/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.resources;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CellTable;
import com.github.gwtbootstrap.client.ui.Controls;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.support.FilterHelper;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.TextBoxClearable;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.model.client.opal.ResourceFactoryDto;
import org.obiba.opal.web.model.client.opal.ResourceReferenceDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.EDIT_ACTION;
import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.REMOVE_ACTION;

public class ProjectResourcesView extends ViewWithUiHandlers<ProjectResourcesUiHandlers> implements ProjectResourcesPresenter.Display {

  private final Translations translations;

  private final TranslationMessages translationMessages;

  interface Binder extends UiBinder<Widget, ProjectResourcesView> {
  }

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
  CellTable<ResourceReferenceDto> table;

  private List<ResourceReferenceDto> resources;

  private final ListDataProvider<ResourceReferenceDto> dataProvider = new ListDataProvider<ResourceReferenceDto>();

  private ActionsColumn<ResourceReferenceDto> actionsColumn;

  private Map<String, ResourceFactoryDto> resourceFactories;

  @Inject
  public ProjectResourcesView(ProjectResourcesView.Binder uiBinder, Translations translations, TranslationMessages translationMessages) {
    this.translations = translations;
    this.translationMessages = translationMessages;
    initWidget(uiBinder.createAndBindUi(this));
    configureTable();
  }

  @Override
  public void renderResources(List<ResourceReferenceDto> resources, Map<String, ResourceFactoryDto> resourceFactories) {
    this.resourceFactories = resourceFactories;
    filter.setText("");
    this.resources = resources;
    dataProvider.setList(resources);
    pager.firstPage();
    dataProvider.refresh();
    pager.setPagerVisible(dataProvider.getList().size() > pager.getPageSize());
    table.setVisible(!resources.isEmpty());
    pager.setVisible(!resources.isEmpty());
    refresh.setVisible(!resources.isEmpty());
    filterControls.setVisible(!resources.isEmpty());
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
        String indexText = Joiner.on(" ").join(resource.getName(), resource.getResource().getUrl());
        if (FilterHelper.matches(indexText, tokens)) filteredResources.add(resource);
      }
    } else
      filteredResources = resources;
    dataProvider.setList(filteredResources);
    pager.firstPage();
    dataProvider.refresh();
  }

  private void configureTable() {
    table.addColumn(new TextColumn<ResourceReferenceDto>() {
      @Override
      public String getValue(ResourceReferenceDto object) {
        return object.getName();
      }
    }, translations.nameLabel());

    table.addColumn(new TextColumn<ResourceReferenceDto>() {
      @Override
      public String getValue(ResourceReferenceDto object) {
        String key = object.getProvider() + ":" + object.getFactory();
        return resourceFactories.get(key).getTitle();
      }
    }, translations.typeLabel());


    table.addColumn(new TextColumn<ResourceReferenceDto>() {
      @Override
      public String getValue(ResourceReferenceDto object) {
        if (object.hasResource())
          return object.getResource().getUrl();
        return "";
      }
    }, translations.urlLabel());

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
        return new String[]{EDIT_ACTION, REMOVE_ACTION};
      }

      @Override
      public String[] getActions(ResourceReferenceDto value) {
        return new String[]{EDIT_ACTION, REMOVE_ACTION};
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
    renderResources(new ArrayList<ResourceReferenceDto>(), resourceFactories);
  }

}
