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

import org.obiba.opal.web.model.client.opal.GroupDto;

import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * A cell that renders a button and takes a delegate to perform actions on mouseUp.
 */
public class GroupStatusIconActionCell extends IconActionCell<GroupDto> {

  public GroupStatusIconActionCell(IconType iconType, Delegate delegate) {
    super(iconType, delegate);
  }

  public boolean isEnabled(GroupDto groupDto) {
    return false;
  }

  @Override
  public void render(Context context, GroupDto value, SafeHtmlBuilder sb) {

    if(isEnabled(value)) {
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
