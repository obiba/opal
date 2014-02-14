/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import com.github.gwtbootstrap.client.ui.SimplePager;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.Range;

public class SimplePagerPanel extends FlowPanel{

  private final HTML label = new HTML();
  private SimplePager pager;

  public SimplePagerPanel() {
    add(label);
  }

  @Override
  public void add(Widget widget) {
    super.add(widget);

    if(widget instanceof SimplePager) {
      pager = (SimplePager) widget;
    }

  }

  public void setPagerVisible(boolean visible) {
    pager.setVisible(visible);
    label.setVisible(!visible);
    HasRows display = pager.getDisplay();
    if (display.getRowCount() > 0 && label.isVisible()) label.setText(createText());
  }

  protected String createText() {
    // Default text is 1 based.
    NumberFormat formatter = NumberFormat.getFormat("#,###");
    HasRows display = pager.getDisplay();
    Range range = display.getVisibleRange();
    int pageStart = range.getStart() + 1;
    int pageSize = range.getLength();
    int dataSize = display.getRowCount();
    int endIndex = Math.min(dataSize, pageStart + pageSize - 1);
    endIndex = Math.max(pageStart, endIndex);
    boolean exact = display.isRowCountExact();
    return formatter.format(pageStart) + "-" + formatter.format(endIndex) + (exact ? " of " : " of over ") +
        formatter.format(dataSize);
  }
}
