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

  private MenuItemProvider menuProvider;

  private MenuItem menu;

  public MenuItemAuthorizer(MenuItemProvider m) {
    super();
    this.menuProvider = m;
  }

  public MenuItemAuthorizer(final MenuItem m) {
    super();
    this.menuProvider = new MenuItemProvider() {

      @Override
      public MenuItem getMenuItem() {
        return m;
      }

    };
  }

  @Override
  public void beforeAuthorization() {
    menu = menuProvider.getMenuItem();
    if(menu != null) menu.setVisible(false);
  }

  @Override
  public void authorized() {
    if(menu != null) menu.setVisible(true);
  }

  @Override
  public void unauthorized() {
    if(menu != null) menu.setVisible(false);
    // if(menu != null && menu.getParentMenu() != null) menu.getParentMenu().removeItem(menu);
  }

  public interface MenuItemProvider {
    public MenuItem getMenuItem();
  }

}
