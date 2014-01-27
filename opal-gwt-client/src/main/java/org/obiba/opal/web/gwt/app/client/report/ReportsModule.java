/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.report;

import org.obiba.opal.web.gwt.app.client.administration.report.presenter.ReportsAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.report.view.ReportsAdministrationView;
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplateDetailsPresenter;
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplateUpdateModalPresenter;
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportsPresenter;
import org.obiba.opal.web.gwt.app.client.report.view.ReportTemplateDetailsView;
import org.obiba.opal.web.gwt.app.client.report.view.ReportTemplateUpdateModalView;
import org.obiba.opal.web.gwt.app.client.report.view.ReportsView;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class ReportsModule extends AbstractPresenterModule {

  @Override
  protected void configure() {
    bindPresenter(ReportsAdministrationPresenter.class, ReportsAdministrationPresenter.Display.class,
        ReportsAdministrationView.class, ReportsAdministrationPresenter.Proxy.class);
    bindPresenterWidget(ReportsPresenter.class, ReportsPresenter.Display.class, ReportsView.class);
    bindPresenterWidget(ReportTemplateDetailsPresenter.class, ReportTemplateDetailsPresenter.Display.class,
        ReportTemplateDetailsView.class);
    bindPresenterWidget(ReportTemplateUpdateModalPresenter.class, ReportTemplateUpdateModalPresenter.Display.class,
        ReportTemplateUpdateModalView.class);
  }
}
