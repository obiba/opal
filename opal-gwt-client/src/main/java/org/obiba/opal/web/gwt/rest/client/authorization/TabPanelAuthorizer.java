/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.rest.client.authorization;

import com.github.gwtbootstrap.client.ui.TabPanel;

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
    tabs.getWidget(index).setVisible(false);
  }

  @Override
  public void authorized() {
    tabs.getWidget(index).setVisible(true);
    // restore the previous tab selection
    if(selectedIndex == index) {
      tabs.selectTab(index);
    }
  }

  @Override
  public void unauthorized() {
    tabs.getWidget(index).setVisible(false);
  }
}
