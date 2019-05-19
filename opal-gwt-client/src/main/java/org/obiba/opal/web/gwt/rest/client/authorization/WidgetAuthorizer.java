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

import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class WidgetAuthorizer implements HasAuthorization {

  private final Widget[] widgets;

  private final boolean visibility;

  public WidgetAuthorizer(Widget... widgets) {
    this(true, widgets);
  }

  public WidgetAuthorizer(boolean visibility, Widget... widgets) {
    this.widgets = widgets;
    this.visibility = visibility;
  }

  private boolean canEnable(Widget w) {
    return !visibility && w instanceof HasEnabled;
  }

  private void toggleWidget(Widget w, boolean toggle) {
    if(canEnable(w)) {
      ((HasEnabled) w).setEnabled(toggle);
    } else {
      w.setVisible(toggle);
    }
  }

  @Override
  public void beforeAuthorization() {
    for(Widget w : widgets)
      toggleWidget(w, false);
  }

  @Override
  public void authorized() {
    for(Widget w : widgets)
      toggleWidget(w, true);
  }

  @Override
  public void unauthorized() {
    for(Widget w : widgets)
      toggleWidget(w, false);
  }

}
