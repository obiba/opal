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

public abstract class URLColumn<T> extends Column<T, String> {

  public URLColumn() {
    super(new HTMLCell());
  }

  @Override
  public String getValue(T object) {
    String url = getURL(object);
    if (!Strings.isNullOrEmpty(url)) {
      String urlTxt = url.length() > 50 ? url.substring(0, 50) + " ..." : url;
      if (url.startsWith("http://") || url.startsWith("https://")) {
        return "<a href='" + url + "' target='_blank' title='" + url + "'>" + urlTxt + "</a>";
      } else if (!urlTxt.equals(url)) {
        return "<span title='" + url + "'>" + urlTxt + "</span>";
      } else {
        return url;
      }
    }
    return "";
  }

  protected abstract String getURL(T object);
}
