/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.report.view;

import org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplatePresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.SplitPaneWorkbenchPresenter;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.UIObjectAuthorizer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

public class ReportTemplateView extends Composite implements ReportTemplatePresenter.Display {

  @UiField
  Button reportTemplateButton;

  @UiField
  Button refreshButton;

  @UiField
  ScrollPanel reportTemplateDetailsPanel;

  @UiField
  ScrollPanel reportTemplateListPanel;

  @UiTemplate("ReportTemplateView.ui.xml")
  interface ReportTemplateViewUiBinder extends UiBinder<Widget, ReportTemplateView> {}

  private static final ReportTemplateViewUiBinder uiBinder = GWT.create(ReportTemplateViewUiBinder.class);

  public ReportTemplateView() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void addToSlot(Object slot, Widget content) {
  }

  @Override
  public void removeFromSlot(Object slot, Widget content) {
  }

  @Override
  public void setInSlot(Object slot, Widget content) {
    HasWidgets panel = null;
    SplitPaneWorkbenchPresenter.Slot splitSlot = (SplitPaneWorkbenchPresenter.Slot) slot;
    switch(splitSlot) {
      case LEFT:
        panel = reportTemplateListPanel;
        break;
      case CENTER:
        panel = reportTemplateDetailsPanel;
        break;
    }
    if(panel != null) {
      panel.clear();
      if(content != null) {
        panel.add(content);
      }
    }
  }

  @Override
  public HandlerRegistration addReportTemplateClickHandler(ClickHandler handler) {
    return reportTemplateButton.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration refreshClickHandler(ClickHandler handler) {
    return refreshButton.addClickHandler(handler);
  }

  @Override
  public HasAuthorization getAddReportTemplateAuthorizer() {
    return new UIObjectAuthorizer(reportTemplateButton);
  }

}
