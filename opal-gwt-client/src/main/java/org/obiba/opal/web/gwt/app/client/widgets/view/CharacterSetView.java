/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.widgets.view;

import org.obiba.opal.web.gwt.app.client.widgets.presenter.CharacterSetDisplay;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;

public class CharacterSetView extends Composite implements CharacterSetDisplay {

  //
  // Static variables
  //

  private EditableListBox charsetListBox = new EditableListBox();

  //
  // Constructors
  //

  public CharacterSetView() {
    for(String s : new String[] { "ISO-8859-1", "ISO-8859-2", "ISO-8859-3", "ISO-8859-4", "ISO-8859-5", "ISO-8859-6", "ISO-8859-7", "ISO-8859-8", "ISO-8859-9", "ISO-8859-13", "ISO-8859-15", "UTF-8", "UTF-16", "UTF-32" }) {
      charsetListBox.addItem(s);
    }
    FlowPanel layout = new FlowPanel();

    layout.add(charsetListBox);
    initWidget(layout);
  }

  @Override
  public void addToSlot(Object slot, Widget content) {
  }

  @Override
  public void removeFromSlot(Object slot, Widget content) {

  }

  @Override
  public void setInSlot(Object slot, Widget content) {

  }

  @Override
  public HasText getCharsetText() {
    return charsetListBox;
  }

  @Override
  public void setDefaultCharset(String defaultCharset) {
    charsetListBox.setText(defaultCharset);
  }

  //
  // Methods
  //

  //
  // Inner Classes / Interfaces
  //
}
