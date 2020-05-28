/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.support;

import com.github.gwtbootstrap.client.ui.TabLink;
import com.github.gwtbootstrap.client.ui.TabPanel;
import com.github.gwtbootstrap.client.ui.base.ListItem;
import com.github.gwtbootstrap.client.ui.base.UnorderedList;

public class TabPanelHelper {

  private TabPanelHelper() {}

  /**
   * Set the tab text if tab is a link.
   * @param tabs
   * @param i
   * @param text
   */
  public static void setTabText(TabPanel tabs, int i, String text) {
    ((TabLink)getNavTab(tabs, i)).setText(text);
  }

  /**
   * Set the title of a tab.
   * @param tabs
   * @param i
   * @param title
   */
  public static void setTabTitle(TabPanel tabs, int i, String title) {
    getNavTab(tabs, i).setTitle(title);
  }

  /**
   * Set a tab visible.
   * @param tabs
   * @param i
   * @param visible
   */
  public static void setTabVisible(TabPanel tabs, int i, boolean visible) {
    getNavTab(tabs, i).setVisible(visible);
  }

  /**
   * Set a tab active.
   * @param tabs
   * @param i
   * @param visible
   */
  public static void setTabActive(TabPanel tabs, int i, boolean active) {
    ((TabLink)getNavTab(tabs, i)).setActive(active);
  }

  private static ListItem getNavTab(TabPanel tabs, int i) {
    UnorderedList ul = (UnorderedList) tabs.getWidget(0);
    return (ListItem) ul.getWidget(i);
  }

}
