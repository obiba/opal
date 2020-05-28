/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.plugins;

import com.gwtplatform.mvp.client.UiHandlers;

public interface PluginsAdministrationUiHandlers extends UiHandlers {

  void getInstalledPlugins();

  void getAvailablePlugins();

  void getUpdatablePlugins();

  void onUninstall(String name);

  void onCancelUninstall(String name);

  void onInstall(String name, String version);

  void onRestart(String name);

  void onConfigure(String name);

  void onPluginFileSelection();
}
