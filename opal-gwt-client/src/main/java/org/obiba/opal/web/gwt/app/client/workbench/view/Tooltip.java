/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.workbench.view;

import java.util.Iterator;

import org.obiba.opal.web.gwt.app.client.view.FadeAnimation;
import org.obiba.opal.web.gwt.app.client.view.FadeAnimation.FadedHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Display tooltips.
 */
public class Tooltip extends Composite implements HasWidgets {

  @UiTemplate("Tooltip.ui.xml")
  interface ViewUiBinder extends UiBinder<PopupPanel, Tooltip> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  @UiField
  PopupPanel dialog;

  @UiField
  Label caption;

  @UiField
  ScrollPanel contentPanel;

  @UiField
  Anchor okay;

  @UiField
  ResizeHandle resizeHandle;

  @UiField
  DockLayoutPanel contentLayout;

  private boolean sticky = true;

  private Timer nonStickyTimer;

  private double opacity = 0.95;

  private boolean animated = true;

  public Tooltip() {
    uiBinder.createAndBindUi(this);

    // Error dialog is initially hidden.
    dialog.hide();
    dialog.setGlassEnabled(false);

    okay.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        dialog.hide();
      }
    });

    resizeHandle.makeResizable(contentLayout, 100, 100);
  }

  public void setPopupPosition(int left, int top) {
    dialog.setPopupPosition(left, top);
  }

  public void setAutoHideEnabled(boolean autoHide) {
    dialog.setAutoHideEnabled(autoHide);
  }

  public void setModal(boolean modal) {
    dialog.setModal(modal);
  }

  public void setAnimationEnabled(boolean animated) {
    this.animated = animated;
  }

  public void setResizable(boolean resizable) {
    resizeHandle.setVisible(resizable);
  }

  public void setSize(String width, String height) {
    contentLayout.setSize(width, height);
  }

  @Override
  public void setWidth(String width) {
    contentLayout.setWidth(width);
  }

  @Override
  public void setHeight(String height) {
    contentLayout.setHeight(height);
  }

  public void setOpacity(double opacity) {
    this.opacity = opacity;
  }

  public void show() {
    if(animated) {
      FadeAnimation.create(dialog.getElement()).from(0).to(opacity).start();
    }
    dialog.show();
    if(!sticky) {
      nonStickyTimer = new Timer() {

        @Override
        public void run() {
          if(dialog.isShowing()) {
            if(animated) {
              FadeAnimation.create(dialog.getElement()).from(opacity).to(0).then(new FadedHandler() {

                @Override
                public void onFaded(Element element) {
                  dialog.hide();
                }
              }).start();
            } else {
              dialog.hide();
            }
          }
        }
      };
      nonStickyTimer.schedule(5000);
    } else {
      nonStickyTimer = null;
    }
  }

  public void hide() {
    dialog.hide();
  }

  public void setCaption(String txt) {
    caption.setText(txt);
  }

  public void setSticky(boolean sticky) {
    this.sticky = sticky;
  }

  @Override
  public void add(Widget w) {
    contentPanel.add(w);
  }

  @Override
  public void clear() {
    contentPanel.clear();
  }

  @Override
  public Iterator<Widget> iterator() {
    return contentPanel.iterator();
  }

  @Override
  public boolean remove(Widget w) {
    return contentPanel.remove(w);
  }

}
