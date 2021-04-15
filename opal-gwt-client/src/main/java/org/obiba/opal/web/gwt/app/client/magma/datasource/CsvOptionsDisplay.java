/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.datasource;

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;

import com.github.gwtbootstrap.client.ui.base.HasType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.gwt.user.client.ui.HasText;
import com.gwtplatform.mvp.client.View;
import org.obiba.opal.web.gwt.app.client.presenter.HasPageTitle;

public interface CsvOptionsDisplay extends View {

  enum CsvFormField {
    FILE,
    ROW,
    FIELD,
    QUOTE,
    CHARSET
  }

  void setCsvFileSelectorVisible(boolean value);

  void setCsvFileSelectorWidgetDisplay(FileSelectionPresenter.Display display);

  HasText getRowText();

  HasText getCharsetText();

  HasText getFieldSeparator();

  HasText getQuote();

  HasText getDefaultValueType();

  HasType<ControlGroupType> getGroupType(String id);

  void setDefaultCharset(String defaultCharset);

  void resetFieldSeparator();

  void resetQuote();

  void resetCommonCharset();

  void clear();
}
