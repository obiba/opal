/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.magma.view;

import com.google.gwt.user.cellview.client.TextColumn;
import org.obiba.opal.web.model.client.magma.TableDto;

/**
 * A column for rendering the reference name of the TableDto.
 */
public class TableReferenceColumn extends TextColumn<TableDto> {
  @Override
  public String getValue(TableDto object) {
    return object.getDatasourceName() + "." + object.getName();
  }
}
