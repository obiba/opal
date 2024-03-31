/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.magma;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import org.obiba.opal.web.model.Magma;

import jakarta.annotation.Nullable;

public interface VariablesViewResource extends VariablesResource {

  @POST
  @Path("/file")
  Response addOrUpdateVariablesFromFile(Magma.ViewDto viewDto, @Nullable String comment);

}
