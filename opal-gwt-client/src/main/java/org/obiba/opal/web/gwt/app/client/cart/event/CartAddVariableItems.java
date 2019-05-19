/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.cart.event;

import com.gwtplatform.dispatch.annotation.GenEvent;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.search.ItemResultDto;

import java.util.List;
import java.util.Map;

@GenEvent
public class CartAddVariableItems {

  String entityType;

  // variables by table full names
  Map<String, List<ItemResultDto>> tableVariables;

}
