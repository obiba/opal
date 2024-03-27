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
import com.google.gwt.user.cellview.client.Column;

public abstract class NameColumn<T> extends Column<T, String> {

  public NameColumn() {
    super(new HTMLCell());
  }

  @Override
  public String getValue(T object) {
    String fullName = getName(object);
    if (!Strings.isNullOrEmpty(fullName)) {
      String[] tokens = fullName.split("~");
      String name = tokens[0];
      if (tokens.length>1) {
        name += " <code title='" + fullName + "' style='font-size: smaller;'>" + tokens[1].split("-")[0] + "</code>";
      }
      return name;
    }
    return "";
  }

  protected abstract String getName(T object);
}
