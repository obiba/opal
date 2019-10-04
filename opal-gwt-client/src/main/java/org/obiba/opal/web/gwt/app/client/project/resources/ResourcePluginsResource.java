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

import org.obiba.opal.web.gwt.app.client.support.PluginsResource;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.opal.PluginPackageDto;
import org.obiba.opal.web.model.client.opal.ResourceFactoryDto;

public class ResourcePluginsResource extends PluginsResource {

  @Override
  public String getResource() {
    return UriBuilders.PLUGINS_RESOURCE.create().build();
  }

  public static ResourcePluginsResource getInstance() {
    return new ResourcePluginsResource();
  }

  public static String makeResourceFactoryKey(PluginPackageDto plugin, ResourceFactoryDto factory) {
    return plugin.getName() + ":" + factory.getName();
  }

  public static String[] splitResourceFactoryKey(String key) {
    return key.split(":");
  }

}
