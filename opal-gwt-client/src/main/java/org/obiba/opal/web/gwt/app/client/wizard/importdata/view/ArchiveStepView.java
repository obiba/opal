/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importdata.view;

import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter.Display;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.ArchiveStepPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

public class ArchiveStepView extends ViewImpl implements ArchiveStepPresenter.Display {

  @UiTemplate("ArchiveStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, ArchiveStepView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private final Widget widget;

  @UiField
  RadioButton archiveLeave;

  @UiField
  RadioButton archiveMove;

  @UiField
  SimplePanel archivePanel;

  private FileSelectionPresenter.Display archiveSelection;

  public ArchiveStepView() {
    widget = uiBinder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public boolean isArchiveLeave() {
    return archiveLeave.getValue();
  }

  @Override
  public boolean isArchiveMove() {
    return archiveMove.getValue();
  }

  @Override
  public void setArchiveWidgetDisplay(Display display) {
    archivePanel.setWidget(display.asWidget());
    archiveSelection = display;
    archiveSelection.setEnabled(isArchiveMove());
    archiveSelection.setFieldWidth("20em");
  }

  @Override
  public String getArchiveDirectory() {
    return isArchiveMove() ? archiveSelection.getFile() : null;
  }

  @Override
  public HandlerRegistration addArchiveLeaveClickHandler(ClickHandler handler) {
    return archiveLeave.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addArchiveMoveClickHandler(ClickHandler handler) {
    return archiveMove.addClickHandler(handler);
  }
}
