/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.workbench.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;

/**
 *
 */
public class ToggleAnchor extends Anchor {

  private static Translations translations = GWT.create(Translations.class);

  /**
   * The delegate that will handle events from the toggle anchor.
   */
  public static interface Delegate {
    /**
     * Perform the desired on/off action.
     *
     * @param event
     */
    void executeOn();

    void executeOff();
  }

  private String onText;

  private String offText;

  private Delegate delegate;

  public ToggleAnchor() {
    super();

    addStyleName("label");

    setText("on");
    setOnText("on");
    setOffText("off");

    addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        if(isOn()) {
          if(delegate != null) {
            delegate.executeOn();
          }
          setText(offText);
        } else {
          if(delegate != null) {
            delegate.executeOff();
          }
          setText(onText);
        }
      }
    });
  }

  public void setDelegate(Delegate delegate) {
    this.delegate = delegate;
  }

  public boolean isOn() {
    return getText().equals(onText);
  }

  public void setOn(boolean on) {
    setOn(on, false);
  }

  public void setOn(boolean on, boolean fire) {
    if(on) {
      if(fire && isOn() == false && delegate != null) {
        delegate.executeOff();
      }
      setText(onText);
    } else {
      if(fire && isOn() && delegate != null) {
        delegate.executeOn();
      }
      setText(offText);
    }
  }

  public void setOnText(String onText) {
    if(isOn()) {
      setText(onText);
    }
    this.onText = onText;
  }

  public void setOffText(String offText) {
    if(isOn() == false) {
      setText(offText);
    }
    this.offText = offText;
  }

  public void setTexts(String onText, String offText) {
    setOnText(onText);
    setOffText(offText);
  }

  public void setShowHideTexts() {
    setOnText(translations.showLabel());
    setOffText(translations.hideLabel());
  }

}
