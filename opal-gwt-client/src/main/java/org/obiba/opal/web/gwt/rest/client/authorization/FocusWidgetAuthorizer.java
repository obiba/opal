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

import com.google.gwt.user.client.ui.FocusWidget;

/**
 *
 */
public class FocusWidgetAuthorizer implements HasAuthorization {

  private FocusWidget w;

  public FocusWidgetAuthorizer(FocusWidget w) {
    super();
    this.w = w;
  }

  @Override
  public void beforeAuthorization() {
    w.setEnabled(false);
  }

  @Override
  public void authorized() {
    w.setEnabled(true);
  }

  @Override
  public void unauthorized() {
    w.setEnabled(false);
  }

}
