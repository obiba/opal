/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.ui.celltable;

import com.google.common.base.Strings;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * A cell that renders a icon.
 *
 * @param <C> the type that this Cell represents
 */
public abstract class LinkCell<C> extends AbstractCell<C> {

  public LinkCell() {
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
   * Get the href attribute value.
   *
   * @param value
   * @return
   */
  public abstract String getLink(C value);

  /**
   * Get the text of the link.
   * @param value
   * @return
   */
  public String getText(C value) {
    return value == null ? "" : value.toString();
  }

  /**
   * Get the icon representing the value.
   * @param value Icon class name, can be null or empty
   * @return
   */
  public String getIcon(C value) {
    return "";
  }

  public String getTarget(C value) {
    return "";
  }

  public String getLinkClass(C value) {
    return "";
  }

  public boolean displayTextWhenNoLink(C value) {
    return true;
  }

  @Override
  public void render(Context context, C value, SafeHtmlBuilder sb) {
    String link = getLink(value);
    String iconClass = getIcon(value);
    if (iconClass == null) iconClass = "";
    if (!iconClass.isEmpty()) iconClass = "<i class='" + iconClass + "'></i> ";
    if(isEnabled() && !Strings.isNullOrEmpty(link)) {
      String target = getTarget(value);
      if (!Strings.isNullOrEmpty(target))
        target = "target='" + target + "'";
      else
        target = "";
      String linkClass = getLinkClass(value);
      if (!Strings.isNullOrEmpty(linkClass))
        linkClass = "class='" + linkClass + "'";
      else
        linkClass = "";
      sb.append(SafeHtmlUtils.fromSafeConstant("<a href='" + link + "' " + target + " " + linkClass + ">"))
          .append(SafeHtmlUtils.fromSafeConstant(iconClass))
          .appendHtmlConstant(getText(value))
          .append(SafeHtmlUtils.fromSafeConstant("</a>"));
    } else if (displayTextWhenNoLink(value)) {
      sb.append(SafeHtmlUtils.fromSafeConstant(getText(value)));
    }
  }
}
