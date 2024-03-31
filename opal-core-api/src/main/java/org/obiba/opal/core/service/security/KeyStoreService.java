/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service.security;

import javax.validation.constraints.NotNull;

import org.obiba.opal.core.security.OpalKeyStore;
import org.obiba.opal.core.service.SystemService;

public interface KeyStoreService extends SystemService {

  void saveKeyStore(@NotNull OpalKeyStore keyStore);

}
