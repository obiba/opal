/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.view;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;

/**
 * Fade an element, default is fade-in.
 */
public class FadeAnimation extends Timer {

  private double opacity;

  private double from = 0;

  private double to = 1;

  private int period = 1;

  private double step = 0.02;

  private Element element;

  private FadedHandler handler;

  private FadeAnimation(Element element) {
    super();
    this.element = element;
  }

  @Override
  public void run() {
    if(from < to && opacity < to) {
      opacity += step;
      applyOpacity();
    } else if(from > to && opacity > to) {
      opacity -= step;
      applyOpacity();
    } else {
      if(handler != null) {
        handler.onFaded(element);
      }
      cancel();
    }
  }

  private void applyOpacity() {
    opacity = Math.max(opacity, 0);
    opacity = Math.min(opacity, 1);
    DOM.setStyleAttribute(element, "opacity", Double.toString(opacity));
  }

  private void start() {
    opacity = from;
    applyOpacity();
    scheduleRepeating(period);
  }

  /**
   * Callback when fading is over.
   */
  public interface FadedHandler {
    public void onFaded(Element element);
  }

  public static class Builder {
    private FadeAnimation fader;

    Builder(Element element) {
      super();
      this.fader = new FadeAnimation(element);
    }

    public Builder from(double from) {
      fader.from = Math.min(from, 1);
      return this;
    }

    public Builder to(double to) {
      fader.to = Math.max(to, 0);
      return this;
    }

    public Builder by(double step) {
      fader.step = step;
      return this;
    }

    public Builder every(int millis) {
      fader.period = millis;
      return this;
    }

    public Builder then(FadedHandler handler) {
      fader.handler = handler;
      return this;
    }

    public FadeAnimation start() {
      fader.start();
      return fader;
    }

  }

  public static Builder create(Element element) {
    Builder builder = new Builder(element);
    return builder;
  }

  public static FadeAnimation start(Element element) {
    return create(element).start();
  }

}
