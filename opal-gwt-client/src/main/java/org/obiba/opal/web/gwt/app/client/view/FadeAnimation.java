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
 *
 */
public class FadeAnimation extends Timer {

  private double opacity = 0;

  private double opacityMax = 0.85;

  private int period = 1;

  private double step = 0.02;

  private Element element;

  private FadeAnimation() {
    super();
  }

  @Override
  public void run() {
    if(opacity < opacityMax) {
      opacity += step;
      setOpacity(opacity);
    } else {
      cancel();
    }
  }

  public FadeAnimation setOpacity(double opacity) {
    this.opacity = opacity;
    DOM.setStyleAttribute(element, "opacity", Double.toString(opacity));
    return this;
  }

  public FadeAnimation setOpacityMax(double opacityMax) {
    this.opacityMax = opacityMax;
    return this;
  }

  public FadeAnimation setPeriod(int period) {
    this.period = period;
    return this;
  }

  public FadeAnimation setStep(double step) {
    this.step = step;
    return this;
  }

  public void start() {
    setOpacity(opacity);
    scheduleRepeating(period);
  }

  public static FadeAnimation create(Element element) {
    FadeAnimation timer = new FadeAnimation();
    timer.element = element;
    return timer;
  }

  public static void start(Element element) {
    create(element).start();
  }

}
