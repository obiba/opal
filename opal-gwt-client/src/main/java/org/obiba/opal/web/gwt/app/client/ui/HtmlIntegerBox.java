/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import com.google.gwt.dom.client.Document;
import com.google.gwt.text.client.IntegerParser;
import com.github.gwtbootstrap.client.ui.ValueBox;

/**
 * Integer input box without number formatting and based on HTML number input element.
 */
public class HtmlIntegerBox extends ValueBox<Integer> {
  public HtmlIntegerBox() {
    super(Document.get().createTextInputElement(), HtmlIntegerRenderer.instance(),
        IntegerParser.instance());
    getElement().setAttribute("type", "number");
    getElement().setAttribute("step", "1");
  }

}
