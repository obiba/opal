/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import javax.validation.constraints.NotNull;

import org.obiba.opal.core.domain.OpalGeneralConfig;

public interface OpalGeneralConfigService extends SystemService {

  void save(@NotNull OpalGeneralConfig config);

  @NotNull
  OpalGeneralConfig getConfig() throws OpalGeneralConfigMissingException;
}
