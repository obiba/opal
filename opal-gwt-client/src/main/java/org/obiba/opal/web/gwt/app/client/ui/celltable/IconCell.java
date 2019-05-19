/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.ui.celltable;

import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * A cell that renders a icon.
 *
 * @param <C> the type that this Cell represents
 */
public abstract class IconCell<C> extends AbstractCell<C> {

  public IconCell() {
  }

  /**
   * Method to be overridden in order to enable dynamically the icon. Default is enabled.
   *
   * @return
   */
  public boolean isEnabled() {
    return true;
  }

  /**
   * Get the icon type from the value.
   * @param value
   * @return
   */
  public abstract IconType getIconType(C value);

  @Override
  public void render(Context context, C value, SafeHtmlBuilder sb) {
    IconType iconType = getIconType(value);
    if(isEnabled()) {
      if(iconType != null) {
        Icon i = new Icon(iconType);
        sb.append(SafeHtmlUtils.fromSafeConstant("<span class=\"icon\">")).appendHtmlConstant(i.toString())
            .append(SafeHtmlUtils.fromSafeConstant("</span>"));
      } else {
        sb.append(SafeHtmlUtils.fromSafeConstant("<span class=\"icon\">"))
            .append(SafeHtmlUtils.fromSafeConstant("</span>"));
      }
    } else {
      if(iconType != null) {
        Icon i = new Icon(iconType);
        sb.append(SafeHtmlUtils.fromSafeConstant("<span class=\"icon disabled\">")).appendHtmlConstant(i.toString())
            .append(SafeHtmlUtils.fromSafeConstant("</span>"));
      } else {
        sb.append(SafeHtmlUtils.fromSafeConstant("<span class=\"iconb disabled\">"))
            .append(SafeHtmlUtils.fromSafeConstant("</span>"));
      }
    }
  }
}
