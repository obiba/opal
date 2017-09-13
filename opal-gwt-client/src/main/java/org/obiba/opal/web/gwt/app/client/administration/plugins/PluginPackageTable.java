/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.plugins;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.InlineLabel;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.model.client.opal.PluginPackageDto;

public class PluginPackageTable extends Table<PluginPackageDto> {

  public static final String INSTALL_ACTION = "Install";

  public static final String REINSTATE_ACTION = "Reinstate";

  public static final String RESTART_ACTION = "Restart";

  public static final String CONFIGURE_ACTION = "Configure";

  public PluginPackageTable() {
    initColumns();
  }

  public void initInstalledPackagesColumns(ActionHandler<PluginPackageDto> actionHandler) {
    ActionsColumn<PluginPackageDto> actionColumn = new ActionsColumn<PluginPackageDto>(new ActionsProvider<PluginPackageDto>() {
      @Override
      public String[] allActions() {
        return new String[] { RESTART_ACTION, CONFIGURE_ACTION, ActionsColumn.REMOVE_ACTION, REINSTATE_ACTION };
      }

      @Override
      public String[] getActions(PluginPackageDto value) {
        return new String[] { RESTART_ACTION, CONFIGURE_ACTION, value.getUninstalled() ? REINSTATE_ACTION : ActionsColumn.REMOVE_ACTION };
      }
    });
    if (actionHandler != null) actionColumn.setActionHandler(actionHandler);
    addColumn(actionColumn, translations.actionsLabel());
  }

  public void initInstallablePackagesColumns(ActionHandler<PluginPackageDto> actionHandler) {
    ActionsColumn<PluginPackageDto> actionColumn = new ActionsColumn<PluginPackageDto>(INSTALL_ACTION);
    if (actionHandler != null) actionColumn.setActionHandler(actionHandler);
    addColumn(actionColumn, translations.actionsLabel());
  }

  private void initColumns() {
    setPageSize(Table.DEFAULT_PAGESIZE);
    setEmptyTableWidget(new InlineLabel(translations.noPluginsLabel()));
    addColumn(new TextColumn<PluginPackageDto>() {
      @Override
      public String getValue(PluginPackageDto item) {
        return item.getName();
      }
    }, translations.nameLabel());
    setColumnWidth(getColumn(0), 100, com.google.gwt.dom.client.Style.Unit.PX);
    addColumn(new TextColumn<PluginPackageDto>() {
      @Override
      public String getValue(PluginPackageDto item) {
        return item.getType();
      }
    }, translations.typeLabel());
    setColumnWidth(getColumn(1), 100, com.google.gwt.dom.client.Style.Unit.PX);
    addColumn(new TextColumn<PluginPackageDto>() {
      @Override
      public String getValue(PluginPackageDto item) {
        return item.getDescription();
      }
    }, translations.descriptionLabel());
    addColumn(new TextColumn<PluginPackageDto>() {
      @Override
      public String getValue(PluginPackageDto item) {
        return item.getVersion();
      }
    }, translations.versionLabel());
  }
}
