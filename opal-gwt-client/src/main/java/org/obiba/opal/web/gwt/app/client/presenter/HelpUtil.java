/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.presenter;

import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;

import com.google.gwt.user.client.Window;

/**
 *
 */
public class HelpUtil {

  private static final String OPALDOC_URL = "http://opaldoc.obiba.org/en/latest";

  private static final String OPALDOCDEV_URL = "http://opaldoc.obiba.org/en/dev";

  private static final String OPALDOC_USERGUIDE_PAGE = "web-user-guide/index.html";

  private HelpUtil() {}

  public static void openPage() {
    Window.open(makePageUrl(OPALDOC_USERGUIDE_PAGE), "_blank", null);
  }

  public static void openPage(String page) {
    Window.open(makePageUrl(page), "_blank", null);
  }

  private static String getOpalDocUrl() {
    String version = ResourceRequestBuilderFactory.newBuilder().getVersion();
    return version != null && version.contains("SNAPSHOT") ? OPALDOCDEV_URL : OPALDOC_URL;
  }

  private static String makePageUrl(String page) {
    return getOpalDocUrl() + "/" + page;
  }

}
