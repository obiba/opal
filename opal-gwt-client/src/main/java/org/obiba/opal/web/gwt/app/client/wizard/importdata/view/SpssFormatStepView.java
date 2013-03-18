/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
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
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.SpssFormatStepPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

public class SpssFormatStepView extends ViewImpl implements SpssFormatStepPresenter.Display {

  @UiTemplate("SpssFormatStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, SpssFormatStepView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private final Widget widget;

  private Display fileSelection;

  @UiField
  SimplePanel selectSpssFilePanel;

  @UiField
  HTMLPanel help;

  @UiField
  CharacterSetView charsetView;

  @UiField
  TextBox entityType;

  public SpssFormatStepView() {
    widget = uiBinder.createAndBindUi(this);
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
  public Widget asWidget() {
    return widget;
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

  @Override
  public HasText getEntityType() {
    return entityType;
  }


}

