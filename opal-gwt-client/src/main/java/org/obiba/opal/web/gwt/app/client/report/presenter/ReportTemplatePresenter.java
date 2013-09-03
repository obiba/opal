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

import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.presenter.SplitPaneWorkbenchPresenter;
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplateUpdateModalPresenter.Mode;
import org.obiba.opal.web.gwt.app.client.support.DefaultBreadcrumbsBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class ReportTemplatePresenter
    extends SplitPaneWorkbenchPresenter<ReportTemplatePresenter.Display, ReportTemplatePresenter.Proxy> {

  ReportTemplateDetailsPresenter reportTemplateDetailsPresenter;

  ReportTemplateListPresenter reportTemplateListPresenter;

  ModalProvider<ReportTemplateUpdateModalPresenter> reportTemplateUpdateModalPresenterProvider;

  public interface Display extends View, HasBreadcrumbs {

    HandlerRegistration addReportTemplateClickHandler(ClickHandler handler);

    HandlerRegistration refreshClickHandler(ClickHandler handler);

    HasAuthorization getAddReportTemplateAuthorizer();
  }

  @ProxyStandard
  @NameToken(Places.REPORT_TEMPLATES)
  public interface Proxy extends ProxyPlace<ReportTemplatePresenter> {}

  private final DefaultBreadcrumbsBuilder breadcrumbsHelper;

  @Inject
  public ReportTemplatePresenter(Display display, EventBus eventBus, Proxy proxy,
      ReportTemplateDetailsPresenter reportTemplateDetailsPresenter,
      ReportTemplateListPresenter reportTemplateListPresenter,
      ModalProvider<ReportTemplateUpdateModalPresenter> reportTemplateUpdateModalPresenterProvider,
      DefaultBreadcrumbsBuilder breadcrumbsHelper) {
    super(eventBus, display, proxy);
    this.reportTemplateDetailsPresenter = reportTemplateDetailsPresenter;
    this.reportTemplateListPresenter = reportTemplateListPresenter;
    this.breadcrumbsHelper = breadcrumbsHelper;
    this.reportTemplateUpdateModalPresenterProvider = reportTemplateUpdateModalPresenterProvider.setContainer(this);
  }

  @Override
  public String getName() {
    return getTitle();
  }

  @Override
  public void authorize(HasAuthorization authorizer) {
  }

  @Override
  public void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event) {
  }

  @Override
  @TitleFunction
  public String getTitle() {
    return translations.pageReportTemplatePage();
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    breadcrumbsHelper.setBreadcrumbView(getView().getBreadcrumbs()).build();
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
    registerHandler(getView().addReportTemplateClickHandler(new AddReportTemplateClickHandler()));
    registerHandler(getView().refreshClickHandler(new ClickHandler() {

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
      ReportTemplateUpdateModalPresenter presenter = reportTemplateUpdateModalPresenterProvider.get();
      presenter.setDialogMode(Mode.CREATE);
    }

  }

}
