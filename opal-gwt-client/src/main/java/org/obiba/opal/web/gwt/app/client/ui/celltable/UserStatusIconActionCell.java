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

import org.obiba.opal.web.model.client.opal.SubjectCredentialsDto;

import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * A cell that renders a button and takes a delegate to perform actions on mouseUp.
 */
public class UserStatusIconActionCell extends IconActionCell<SubjectCredentialsDto> {

  public UserStatusIconActionCell(IconType iconType, Delegate<SubjectCredentialsDto> delegate) {
    super(iconType, delegate);
  }

  @Override
  public void render(Context context, SubjectCredentialsDto value, SafeHtmlBuilder sb) {
    String cssClass = "icon";
    if(!value.getEnabled()) cssClass += " disabled";
    sb.append(SafeHtmlUtils.fromSafeConstant("<a class=\"" + cssClass + "\">")) //
        .appendHtmlConstant(new Icon(value.getEnabled() ? iconType : IconType.REMOVE).toString()) //
        .append(message) //
        .append(SafeHtmlUtils.fromSafeConstant("</a>"));
  }
}
