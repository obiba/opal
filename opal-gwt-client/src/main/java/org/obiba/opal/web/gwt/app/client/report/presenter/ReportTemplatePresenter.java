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

import org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplateUpdateDialogPresenter.Mode;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class ReportTemplatePresenter extends WidgetPresenter<ReportTemplatePresenter.Display> {

  ReportTemplateDetailsPresenter reportTemplateDetailsPresenter;

  ReportTemplateListPresenter reportTemplateListPresenter;

  Provider<ReportTemplateUpdateDialogPresenter> reportTemplateUpdateDialogPresenterProvider;

  public interface Display extends WidgetDisplay {
    ScrollPanel getReportTemplateDetailsPanel();

    ScrollPanel getReportTemplateListPanel();

    HandlerRegistration addReportTemplateClickHandler(ClickHandler handler);

    HandlerRegistration refreshClickHandler(ClickHandler handler);

    HasAuthorization getAddReportTemplateAuthorizer();
  }

  @Inject
  public ReportTemplatePresenter(final Display display, final EventBus eventBus, ReportTemplateDetailsPresenter reportTemplateDetailsPresenter, ReportTemplateListPresenter reportTemplateListPresenter, Provider<ReportTemplateUpdateDialogPresenter> reportTemplateUpdateDialogPresenter) {
    super(display, eventBus);
    this.reportTemplateDetailsPresenter = reportTemplateDetailsPresenter;
    this.reportTemplateListPresenter = reportTemplateListPresenter;
    this.reportTemplateUpdateDialogPresenterProvider = reportTemplateUpdateDialogPresenter;
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
    authorize();
  }

  @Override
  protected void onBind() {
    addHandlers();
    initDisplayComponents();
  }

  private void addHandlers() {
    super.registerHandler(getDisplay().addReportTemplateClickHandler(new AddReportTemplateClickHandler()));
    super.registerHandler(getDisplay().refreshClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        reportTemplateDetailsPresenter.refreshDisplay();
      }
    }));
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

  private void authorize() {
    // create report templates
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/report-templates").post().authorize(getDisplay().getAddReportTemplateAuthorizer()).send();
  }

  public class AddReportTemplateClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      ReportTemplateUpdateDialogPresenter presenter = reportTemplateUpdateDialogPresenterProvider.get();
      presenter.bind();
      presenter.setDialogMode(Mode.CREATE);
      presenter.getDisplay().setEnabledReportTemplateName(true);
      presenter.revealDisplay();
    }

  }

}
