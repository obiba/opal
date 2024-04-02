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

import java.util.List;

import jakarta.ws.rs.GET;

import org.obiba.magma.support.VariableEntityBean;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.ws.security.NoAuthorization;

public interface VariableEntityTablesResource {

  void setVariableEntity(VariableEntityBean variableEntity);

  @GET
  @NoAuthorization
  List<Magma.TableDto> getTables();

  List<Magma.TableDto> getTables(int limit);
}
