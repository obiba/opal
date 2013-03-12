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

import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplateUpdateDialogPresenter.Mode;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.SplitPaneWorkbenchPresenter;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class ReportTemplatePresenter
    extends SplitPaneWorkbenchPresenter<ReportTemplatePresenter.Display, ReportTemplatePresenter.Proxy> {

  ReportTemplateDetailsPresenter reportTemplateDetailsPresenter;

  ReportTemplateListPresenter reportTemplateListPresenter;

  Provider<ReportTemplateUpdateDialogPresenter> reportTemplateUpdateDialogPresenterProvider;

  public interface Display extends View {

    HandlerRegistration addReportTemplateClickHandler(ClickHandler handler);

    HandlerRegistration refreshClickHandler(ClickHandler handler);

    HasAuthorization getAddReportTemplateAuthorizer();
  }

  @ProxyStandard
  @NameToken(Places.reportTemplates)
  public interface Proxy extends ProxyPlace<ReportTemplatePresenter> {}

  @Inject
  public ReportTemplatePresenter(final Display display, final EventBus eventBus, Proxy proxy,
      ReportTemplateDetailsPresenter reportTemplateDetailsPresenter,
      ReportTemplateListPresenter reportTemplateListPresenter,
      Provider<ReportTemplateUpdateDialogPresenter> reportTemplateUpdateDialogPresenter) {
    super(eventBus, display, proxy);
    this.reportTemplateDetailsPresenter = reportTemplateDetailsPresenter;
    this.reportTemplateListPresenter = reportTemplateListPresenter;
    this.reportTemplateUpdateDialogPresenterProvider = reportTemplateUpdateDialogPresenter;
  }

  @Override
  protected PresenterWidget<?> getDefaultPresenter(SplitPaneWorkbenchPresenter.Slot slot) {
    switch(slot) {
      case LEFT:
        return reportTemplateListPresenter;
      case CENTER:
        return reportTemplateDetailsPresenter;
    }
    return null;
  }

  @Override
  protected void addHandlers() {
    super.registerHandler(getView().addReportTemplateClickHandler(new AddReportTemplateClickHandler()));
    super.registerHandler(getView().refreshClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        reportTemplateDetailsPresenter.refresh();
      }
    }));
  }

  @Override
  protected void authorize() {
    // create report templates
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/report-templates").post()
        .authorize(getView().getAddReportTemplateAuthorizer()).send();
  }

  public class AddReportTemplateClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      ReportTemplateUpdateDialogPresenter presenter = reportTemplateUpdateDialogPresenterProvider.get();
      presenter.setDialogMode(Mode.CREATE);
      addToPopupSlot(presenter);
    }

  }

}
