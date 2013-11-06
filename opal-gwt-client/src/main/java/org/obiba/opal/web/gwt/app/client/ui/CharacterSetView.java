/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.ui;

import java.util.Arrays;

import org.obiba.opal.web.gwt.app.client.presenter.CharacterSetDisplay;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IsWidget;

public class CharacterSetView extends EditableListBox implements CharacterSetDisplay {

  public CharacterSetView() {
    addAllItems(Arrays
        .asList("ISO-8859-1", "ISO-8859-2", "ISO-8859-3", "ISO-8859-4", "ISO-8859-5", "ISO-8859-6", "ISO-8859-7",
            "ISO-8859-8", "ISO-8859-9", "ISO-8859-13", "ISO-8859-15", "UTF-8", "UTF-16", "UTF-32"));
  }

  @Override
  public HasText getCharsetText() {
    return this;
  }

  @Override
  public void setDefaultCharset(String defaultCharset) {
    setText(defaultCharset);
  }
}
