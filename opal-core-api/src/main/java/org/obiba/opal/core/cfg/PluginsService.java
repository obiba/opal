/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.cfg;

import org.obiba.opal.core.domain.plugins.PluginPackage;
import org.obiba.opal.core.service.SystemService;

import java.util.List;

public interface PluginsService extends SystemService {

  String getUpdateSite();

  List<PluginPackage> getInstalledPlugins();

  List<PluginPackage> getUpdatablePlugins();

  List<PluginPackage> getAvailablePlugins();

  void installPlugin(String name, String version);

  boolean restartRequired();

}
