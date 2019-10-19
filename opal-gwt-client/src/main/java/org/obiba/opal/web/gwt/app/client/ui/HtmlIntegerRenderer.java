/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.text.shared.Renderer;

/**
 * No integer formatting, to let HTML number input handle value properly.
 */
public class HtmlIntegerRenderer extends AbstractRenderer<Integer> {

  private static HtmlIntegerRenderer INSTANCE;

  /**
   * Returns the instance.
   */
  public static Renderer<Integer> instance() {
    if (INSTANCE == null) {
      INSTANCE = new HtmlIntegerRenderer();
    }
    return INSTANCE;
  }

  @Override
  public String render(Integer object) {
    if (null == object) {
      return "";
    }
    return object.toString();
  }
}
