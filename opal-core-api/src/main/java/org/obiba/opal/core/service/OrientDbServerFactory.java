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

import com.google.common.annotations.VisibleForTesting;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.server.OServer;
import javax.validation.constraints.NotNull;

public interface OrientDbServerFactory {

  @NotNull
  OServer getServer();

  @NotNull
  ODatabaseDocument getDocumentTx();

  @VisibleForTesting
  void setUrl(@NotNull String url);

  @VisibleForTesting
  void start() throws Exception;

  @VisibleForTesting
  void stop();
}
