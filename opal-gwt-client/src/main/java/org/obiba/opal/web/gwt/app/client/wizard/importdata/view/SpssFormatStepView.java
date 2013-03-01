/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.wizard.importdata.view;

import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter.Display;
import org.obiba.opal.web.gwt.app.client.widgets.view.CharacterSetView;
import org.obiba.opal.web.gwt.app.client.widgets.view.CsvOptionsView;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.SpssFormatStepPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
public class SpssFormatStepView extends Composite implements SpssFormatStepPresenter.Display {

  @UiTemplate("SpssFormatStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, SpssFormatStepView> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private Display fileSelection;

  @UiField
  SimplePanel selectSpssFilePanel;

  @UiField
  HTMLPanel help;

  @UiField
  CharacterSetView charsetView;

  public SpssFormatStepView() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void setSpssFileSelectorWidgetDisplay(Display display) {
    selectSpssFilePanel.setWidget(display.asWidget());
    fileSelection = display;
    fileSelection.setFieldWidth("20em");
  }

  @Override
  public String getSelectedFile() {
    return fileSelection.getFile();
  }

  @Override
  public void addToSlot(Object slot, Widget content) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void removeFromSlot(Object slot, Widget content) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void setInSlot(Object slot, Widget content) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

  @Override
  public Widget getStepHelp() {
    help.removeFromParent();
    return help;
  }

  @Override
  public HasText getCharsetText() {
    return charsetView.getCharsetText();
  }

  @Override
  public void setDefaultCharset(String defaultCharset) {
    charsetView.setDefaultCharset(defaultCharset);
  }

}

