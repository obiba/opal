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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
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
  interface ReportTemplateViewUiBinder extends UiBinder<Widget, ReportTemplateView> {
  }

  private static ReportTemplateViewUiBinder uiBinder = GWT.create(ReportTemplateViewUiBinder.class);

  public ReportTemplateView() {
    initWidget(uiBinder.createAndBindUi(this));
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

  @Override
  public ScrollPanel getReportTemplateDetailsPanel() {
    return reportTemplateDetailsPanel;
  }

  @Override
  public ScrollPanel getReportTemplateListPanel() {
    return reportTemplateListPanel;
  }

  @Override
  public HandlerRegistration addReportTemplateClickHandler(ClickHandler handler) {
    return reportTemplateButton.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration refreshClickHandler(ClickHandler handler) {
    return refreshButton.addClickHandler(handler);
  }

}
