/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.workbench.view;

import com.google.gwt.user.client.ui.FlowPanel;

/**
 *
 */
public class PropertyPanel extends FlowPanel {

  private int column = 0;

  private int span = -1;

  public PropertyPanel() {
    this(0);
  }

  public PropertyPanel(int column) {
    super();
    this.column = column;
  }

  public void setColumn(int column) {
    this.column = column;
  }

  public int getColumn() {
    return column;
  }

  public void setSpan(int span) {
    this.span = span;
  }

  public int getSpan() {
    return span;
  }

}
