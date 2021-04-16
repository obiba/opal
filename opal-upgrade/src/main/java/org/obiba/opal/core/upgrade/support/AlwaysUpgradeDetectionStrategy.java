/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.upgrade.support;

import org.obiba.runtime.upgrade.VersionProvider;
import org.obiba.runtime.upgrade.support.NewInstallationDetectionStrategy;

public class AlwaysUpgradeDetectionStrategy implements NewInstallationDetectionStrategy {

  @Override
  @SuppressWarnings("MethodReturnAlwaysConstant")
  public boolean isNewInstallation(VersionProvider runtimeVersionProvider) {
    return false;
  }

}
