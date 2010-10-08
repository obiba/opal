/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.view;

import org.obiba.opal.web.gwt.app.client.widgets.presenter.CsvOptionsDisplay;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter.Display;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public abstract class AbstractCsvOptionsView extends Composite implements CsvOptionsDisplay {
  //
  // CsvFormatStepPresenter.Display Methods
  //

  @Override
  public void setCsvFileSelectorWidgetDisplay(Display display) {
    getCsvOptions().setCsvFileSelectorWidgetDisplay(display);
  }

  @Override
  public HasText getRowText() {
    return getCsvOptions().getRowText();
  }

  @Override
  public String getFieldSeparator() {
    return getCsvOptions().getFieldSeparator();
  }

  @Override
  public String getQuote() {
    return getCsvOptions().getQuote();
  }

  @Override
  public HasValue<Boolean> isDefaultCharacterSet() {
    return getCsvOptions().isDefaultCharacterSet();
  }

  @Override
  public void setDefaultCharset(String defaultCharset) {
    getCsvOptions().setDefaultCharset(defaultCharset);
  }

  @Override
  public HasValue<Boolean> isCharsetCommonList() {
    return getCsvOptions().isCharsetCommonList();
  }

  @Override
  public String getCharsetCommonList() {
    return getCsvOptions().getCharsetCommonList();
  }

  @Override
  public HasValue<Boolean> isCharsetSpecify() {
    return getCsvOptions().isCharsetSpecify();
  }

  @Override
  public HasText getCharsetSpecifyText() {
    return getCsvOptions().getCharsetSpecifyText();
  }

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

  //
  // Methods
  //

  protected abstract CsvOptionsView getCsvOptions();
}
