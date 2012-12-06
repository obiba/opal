/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.workbench.view;

import com.google.gwt.dom.client.Element;
import com.watopi.chosen.client.ChosenOptions;
import com.watopi.chosen.client.gwt.ChosenListBox;

/**
 * List box based on @{ChosenListBox}.
 */
public class Chooser extends ChosenListBox {

  public Chooser() {
  }

  public Chooser(ChosenOptions options) {
    super(options);
  }

  public Chooser(boolean isMultipleSelect) {
    super(isMultipleSelect);
  }

  public Chooser(boolean isMultipleSelect, ChosenOptions options) {
    super(isMultipleSelect, options);
  }

  public Chooser(Element element) {
    super(element);
  }

  @Override
  public void setItemSelected(int index, boolean selected) {
    super.setItemSelected(index, selected);
    update();
  }

  @Override
  public void removeItem(int index) {
    super.removeItem(index);
    update();
  }
}
