/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.presenter;

import com.google.gwt.user.client.ui.HasText;
import com.gwtplatform.mvp.client.View;

public interface CsvOptionsDisplay extends View {

  void setCsvFileSelectorWidgetDisplay(FileSelectionPresenter.Display display);

  HasText getRowText();

  HasText getCharsetText();

  String getFieldSeparator();

  String getQuote();

  void setDefaultCharset(String defaultCharset);

  void resetFieldSeparator();

  void resetQuote();

  void resetCommonCharset();

  void clear();
}
