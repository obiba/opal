/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.support;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.Chooser;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.model.client.opal.DatasourcePluginPackageDto;
import org.obiba.opal.web.model.client.opal.PluginPackageDto;
import org.obiba.opal.web.model.client.opal.PluginPackagesDto;

public abstract class PluginPackageHelper {

  public static PluginPackageDto findPluginPackage(String name, JsArray<PluginPackageDto> pluginPackageDtoJsArray) {
    boolean found = false;
    int index = 0;

    if (pluginPackageDtoJsArray != null) {
      while (!found && index < pluginPackageDtoJsArray.length()) {
        found = pluginPackageDtoJsArray.get(index).getName().equals(name);
        if (!found) index++;
      }
    }

    return found ? pluginPackageDtoJsArray.get(index) : null;
  }

  public static class PluginPackageResourceCallback implements ResourceCallback<PluginPackagesDto> {

    private JsArray<PluginPackageDto> pluginPackageDtoJsArray;
    private Chooser formatChooser;

    public PluginPackageResourceCallback(JsArray<PluginPackageDto> pluginPackageDtoJsArray, Chooser formatChooser) {
      this.pluginPackageDtoJsArray = pluginPackageDtoJsArray;
      this.formatChooser = formatChooser;
    }

    @Override
    public void onResource(Response response, PluginPackagesDto resource) {
      JsArrays.pushAll(pluginPackageDtoJsArray, JsArrays.toSafeArray(resource.getPackagesArray()));
      for (PluginPackageDto plugin : JsArrays.toIterable(pluginPackageDtoJsArray)) {
        DatasourcePluginPackageDto dsPlugin = plugin.getExtension(DatasourcePluginPackageDto.PluginPackageDtoExtensions.datasource).cast();
        if ("FILE".equals(dsPlugin.getGroup())) {
          formatChooser.addItemToGroup(plugin.getTitle(), plugin.getName(), 0);
        }
        if ("SERVER".equals(dsPlugin.getGroup())) {
          formatChooser.addItemToGroup(plugin.getTitle(), plugin.getName(), 1);
        }
      }
      if (pluginPackageDtoJsArray.length()>0)
        formatChooser.update();
    }
  }
}
