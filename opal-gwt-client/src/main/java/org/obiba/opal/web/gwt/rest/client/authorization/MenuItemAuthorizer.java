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


import com.google.gwt.user.client.ui.MenuItem;

/**
 *
 */
public class MenuItemAuthorizer implements HasAuthorization {

  private MenuItem menu;

  public MenuItemAuthorizer(MenuItem m) {
    super();
    this.menu = m;
  }

  @Override
  public void beforeAuthorization() {
    menu.setVisible(false);
  }

  @Override
  public void authorized() {
    menu.setVisible(true);
  }

  @Override
  public void unauthorized() {
    menu.getParentMenu().removeItem(menu);
  }

}
