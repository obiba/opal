/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.rest.client.authorization;

import org.obiba.opal.web.gwt.app.client.ui.AbstractTabPanel;

/**
 * Authorize a tab in {@link org.obiba.opal.web.gwt.app.client.ui.NavTabsPanel}.
 */
public class TabAuthorizer implements HasAuthorization {

  private final AbstractTabPanel tabs;

  private final int index;

  private int selectedIndex;

  public TabAuthorizer(AbstractTabPanel tabs, int index) {
    this.tabs = tabs;
    this.index = index;
  }

  @Override
  public void beforeAuthorization() {
    // if the tab to hide is the selected one, tab selection changes
    selectedIndex = tabs.getSelectedIndex();
    tabs.setTabVisible(index, false);
  }

  @Override
  public void authorized() {
    tabs.setTabVisible(index, true);
    // restore the previous tab selection
    if(selectedIndex == index) {
      tabs.selectTab(index);
    }
  }

  @Override
  public void unauthorized() {
    tabs.setTabVisible(index, false);
  }
}
