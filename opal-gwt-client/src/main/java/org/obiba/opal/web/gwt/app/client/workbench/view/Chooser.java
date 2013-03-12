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
    initWidget();
  }

  public Chooser(ChosenOptions options) {
    super(options);
    initWidget();
  }

  public Chooser(boolean isMultipleSelect) {
    super(isMultipleSelect);
    initWidget();
  }

  public Chooser(boolean isMultipleSelect, ChosenOptions options) {
    super(isMultipleSelect, options);
    initWidget();
  }

  public Chooser(Element element) {
    super(element);
    initWidget();
  }

  private void initWidget() {
    setDisableSearchThreshold(5);
    setSearchContains(true);
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    update();
  }

  @Override
  public void addItem(String item) {
    super.addItem(item);
    update();
  }

  @Override
  public void addItem(String item, String value) {
    super.addItem(item, value);
    update();
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

  @Override
  public void insertItemToGroup(String item, String value, int groupIndex, int itemIndex) {
    // TODO should be fixed in next gwtchosen release
    insertItemToGroup(item, null, value, groupIndex, itemIndex);
  }

  public String getSelectedValue() {
    return getValue(getSelectedIndex());
  }

  public void setSelectedValue(String value) {
    for(int i = 0; i < getItemCount(); i++) {
      if(getValue(i).equals(value)) {
        super.setItemSelected(i, true);
      }
    }
    update();
  }
}
