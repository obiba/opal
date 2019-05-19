/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.rest.client.authorization;

import com.github.gwtbootstrap.client.ui.TabPanel;
import com.github.gwtbootstrap.client.ui.base.ListItem;
import com.github.gwtbootstrap.client.ui.base.UnorderedList;

/**
 * Authorize a tab panel.
 */
public class TabPanelAuthorizer implements HasAuthorization {

  private final TabPanel tabs;

  private final int index;

  private int selectedIndex;

  public TabPanelAuthorizer(TabPanel tabs, int index) {
    this.tabs = tabs;
    this.index = index;
  }

  @Override
  public void beforeAuthorization() {
    // if the tab to hide is the selected one, tab selection changes
    selectedIndex = tabs.getSelectedTab();
    getNavTab(index).setVisible(false);
  }

  @Override
  public void authorized() {
    getNavTab(index).setVisible(true);
    // restore the previous tab selection
    if(selectedIndex == index) {
      tabs.selectTab(index);
    }
  }

  @Override
  public void unauthorized() {
    getNavTab(index).setVisible(false);
    if(selectedIndex == index) {
      tabs.selectTab(index + 1);
    }
  }

  private ListItem getNavTab(int i) {
    UnorderedList ul = (UnorderedList) tabs.getWidget(0);
    return (ListItem) ul.getWidget(i);
  }
}
