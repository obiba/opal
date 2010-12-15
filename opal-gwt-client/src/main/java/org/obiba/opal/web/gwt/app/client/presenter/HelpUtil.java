/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.presenter;

import com.google.gwt.user.client.Window;

/**
 *
 */
public class HelpUtil {

  private static String OPALDOC_URL = "http://wiki.obiba.org/confluence/display/OPALDOCDEV";

  private static String OPALDOC_USERGUIDE_PAGE = "Opal+Web+Application+User+Guide";

  public static void openPage() {
    Window.open(makePageUrl(OPALDOC_USERGUIDE_PAGE), "_blank", null);
  }

  public static void openPage(String page) {
    Window.open(makePageUrl(page), "_blank", null);
  }

  private static String makePageUrl(String page) {
    return OPALDOC_URL + "/" + page;
  }

}
