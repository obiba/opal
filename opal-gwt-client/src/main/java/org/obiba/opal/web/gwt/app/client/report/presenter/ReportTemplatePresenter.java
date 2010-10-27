/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.report.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.inject.Inject;

public class ReportTemplatePresenter extends WidgetPresenter<ReportTemplatePresenter.Display> {

  ReportTemplateDetailsPresenter reportTemplateDetailsPresenter;

  ReportTemplateListPresenter reportTemplateListPresenter;

  public interface Display extends WidgetDisplay {
    ScrollPanel getReportTemplateDetailsPanel();

    ScrollPanel getReportTemplateListPanel();
  }

  @Inject
  public ReportTemplatePresenter(final Display display, final EventBus eventBus, ReportTemplateDetailsPresenter reportTemplateDetailsPresenter, ReportTemplateListPresenter reportTemplateListPresenter) {
    super(display, eventBus);
    this.reportTemplateDetailsPresenter = reportTemplateDetailsPresenter;
    this.reportTemplateListPresenter = reportTemplateListPresenter;
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
  }

  @Override
  protected void onBind() {
    initDisplayComponents();
  }

  protected void initDisplayComponents() {

    getDisplay().getReportTemplateDetailsPanel().add(reportTemplateDetailsPresenter.getDisplay().asWidget());
    getDisplay().getReportTemplateListPanel().add(reportTemplateListPresenter.getDisplay().asWidget());

    reportTemplateListPresenter.bind();
    reportTemplateDetailsPresenter.bind();
  }

  @Override
  protected void onUnbind() {
    getDisplay().getReportTemplateDetailsPanel().remove(reportTemplateDetailsPresenter.getDisplay().asWidget());
    getDisplay().getReportTemplateListPanel().remove(reportTemplateListPresenter.getDisplay().asWidget());

    reportTemplateListPresenter.unbind();
    reportTemplateDetailsPresenter.unbind();
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

}
