/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.celltable;

import org.obiba.opal.web.model.client.opal.UserDto;

import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * A cell that renders a button and takes a delegate to perform actions on mouseUp.
 */
public class UserStatusIconActionCell extends IconActionCell<UserDto> {

  public UserStatusIconActionCell(IconType iconType, Delegate delegate) {
    super(iconType, delegate);
  }

  @Override
  public void render(Context context, UserDto value, SafeHtmlBuilder sb) {

    if(value.getEnabled()) {
      Icon i = new Icon(iconType);
      sb.append(SafeHtmlUtils.fromSafeConstant("<a class=\"icon\">")).appendHtmlConstant(i.toString()).append(message)
          .append(SafeHtmlUtils.fromSafeConstant("</a>"));

    } else {
      Icon i = new Icon(IconType.REMOVE);
      sb.append(SafeHtmlUtils.fromSafeConstant("<a class=\"icon disabled\">")).appendHtmlConstant(i.toString())
          .append(message).append(SafeHtmlUtils.fromSafeConstant("</a>"));
    }
  }

}
