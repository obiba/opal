/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.authz.view;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class EnablableCheckboxCell extends CheckboxCell {

  /**
   * An html string representation of a checked input box.
   */
  private static final SafeHtml INPUT_DISABLED_CHECKED = SafeHtmlUtils.fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" checked disabled=\"disabled\"/>");

  /**
   * An html string representation of an unchecked input box.
   */
  private static final SafeHtml INPUT_DISABLED_UNCHECKED = SafeHtmlUtils.fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" disabled=\"disabled\"/>");

  private boolean enabled;

  public EnablableCheckboxCell() {
    this(true);
  }

  public EnablableCheckboxCell(boolean enabled) {
    this.enabled = enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public void render(com.google.gwt.cell.client.Cell.Context context, Boolean value, SafeHtmlBuilder sb) {
    if(enabled) {
      super.render(context, value, sb);
    } else {
      // Get the view data.
      Object key = context.getKey();
      Boolean viewData = getViewData(key);
      if(viewData != null && viewData.equals(value)) {
        clearViewData(key);
        viewData = null;
      }

      if(value != null && ((viewData != null) ? viewData : value)) {
        sb.append(INPUT_DISABLED_CHECKED);
      } else {
        sb.append(INPUT_DISABLED_UNCHECKED);
      }
    }
  }
}