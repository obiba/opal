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
import com.github.gwtbootstrap.client.ui.constants.IconSize;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;

public class ClickableIconRenderer extends AbstractSafeHtmlRenderer<String> {

  private final IconType iconType;

  public ClickableIconRenderer(IconType iconType) {
    this.iconType = iconType;
  }

  @Override
  public SafeHtml render(String object) {
    if(object == null || object.trim().isEmpty()) return new SafeHtmlBuilder().toSafeHtml();

    Icon i = new Icon(iconType);
    i.setIconSize(IconSize.LARGE);
    i.addStyleName("xsmall-right-indent");
    return new SafeHtmlBuilder().appendHtmlConstant("<a class=\"iconb\">").appendHtmlConstant(i.toString())
        .appendEscaped(object).appendHtmlConstant("</a>").toSafeHtml();
  }
}