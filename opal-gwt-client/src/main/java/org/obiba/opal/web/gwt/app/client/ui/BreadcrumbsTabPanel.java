/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.ui;

import com.github.gwtbootstrap.client.ui.Breadcrumbs;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class BreadcrumbsTabPanel extends AbstractTabPanel {

  public BreadcrumbsTabPanel() {
    super(new Breadcrumbs());

    // remove all tabs after the one selected
    addSelectionHandler(new SelectionHandler<Integer>() {

      @Override
      public void onSelection(SelectionEvent<Integer> event) {
        final int idx = event.getSelectedItem();
        if(isAnimationEnabled()) {
          // wait for the end of the animation before removing descendants
          Timer timer = new Timer() {
            @Override
            public void run() {
              if(!isAnimationRunning()) {
                removeDescendants(idx);
                cancel();
              }
            }
          };
          timer.scheduleRepeating(10);
        } else {
          removeDescendants(idx);
        }
      }

      private void removeDescendants(int idx) {
        while(getWidgetCount() > idx + 1) {
          remove(getWidgetCount() - 1);
        }
      }
    });

    setAnimationEnabled(true);
    getMenu().addStyleName("inline-block");
  }

  @Override
  public void setAnimationEnabled(boolean enable) {
    super.setAnimationEnabled(enable);
  }

  @Override
  public boolean isAnimationEnabled() {
    return super.isAnimationEnabled();
  }

  public void addAndSelect(Widget widget, String text) {
    add(widget, text);
    selectTab(widget);
  }

  public void addAndSelect(Widget widget, HasClickHandlers link) {
    add(widget, link);
    selectTab(widget);
  }

}
