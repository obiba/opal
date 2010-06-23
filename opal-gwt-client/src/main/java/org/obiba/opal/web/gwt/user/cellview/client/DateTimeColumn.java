/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.user.cellview.client;

import java.util.Date;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.cellview.client.Column;

public abstract class DateTimeColumn<T> extends Column<T, Date> {
  public DateTimeColumn() {
    super(new DateCell(DateTimeFormat.getShortDateTimeFormat()));
  }

  public DateTimeColumn(DateTimeFormat dateFormat) {
    super(new DateCell(dateFormat));
  }
}
