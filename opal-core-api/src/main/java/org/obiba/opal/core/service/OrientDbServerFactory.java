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

import jakarta.validation.constraints.NotNull;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.server.OServer;

public interface OrientDbServerFactory {

  @NotNull
  OServer getServer();

  @NotNull
  ODatabaseDocumentTx getDocumentTx();

  void setUrl(@NotNull String url);
}
