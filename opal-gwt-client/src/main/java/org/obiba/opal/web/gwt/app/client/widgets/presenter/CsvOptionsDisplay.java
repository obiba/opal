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

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import com.google.gwt.user.client.ui.HasText;

/**
 *
 */
public interface CsvOptionsDisplay extends WidgetDisplay {

  void setCsvFileSelectorWidgetDisplay(FileSelectionPresenter.Display display);

  void setDefaultCharset(String defaultCharset);

  boolean isDefaultCharacterSet();

  boolean isCharsetCommonList();

  String getCharsetCommonList();

  String getFieldSeparator();

  String getQuote();

  HasText getRowText();

  boolean isCharsetSpecify();

  HasText getCharsetSpecifyText();
}
