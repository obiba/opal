/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.r;

import com.gwtplatform.mvp.client.UiHandlers;
import org.obiba.opal.web.model.client.opal.r.RPackageDto;

public interface RAdministrationUiHandlers extends UiHandlers {

  void start();

  void stop();

  void test();

  void onRemovePackage(RPackageDto rPackage);

  void onRefreshPackages();

  void onInstallPackage();

  void onUpdatePackages();

  void onDownloadRserveLog();
}
