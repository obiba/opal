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

import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.InlineLabel;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.model.client.opal.PluginPackageDto;

public class PluginPackageTable extends Table<PluginPackageDto> {

  public PluginPackageTable() {
    initColumns();
  }

  private void initColumns() {
    setPageSize(Table.DEFAULT_PAGESIZE);
    setEmptyTableWidget(new InlineLabel(translations.noPluginsLabel()));
    addColumn(new TextColumn<PluginPackageDto>() {
      @Override
      public String getValue(PluginPackageDto item) {
        return item.getType();
      }
    }, translations.typeLabel());
    addColumn(new TextColumn<PluginPackageDto>() {
      @Override
      public String getValue(PluginPackageDto item) {
        return item.getName();
      }
    }, translations.nameLabel());
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
