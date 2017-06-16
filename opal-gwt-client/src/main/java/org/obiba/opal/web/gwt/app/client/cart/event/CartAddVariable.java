/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.cart.event;

import com.gwtplatform.dispatch.annotation.GenEvent;

@GenEvent
public class CartAddVariable {

  String entityType;

  String datasource;

  String table;

  String variable;

}
