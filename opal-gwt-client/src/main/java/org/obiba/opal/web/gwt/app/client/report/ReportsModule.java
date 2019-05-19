/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.report;

import org.obiba.opal.web.gwt.app.client.administration.report.ReportsAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.report.ReportsAdministrationView;
import org.obiba.opal.web.gwt.app.client.report.edit.ReportTemplateEditModalPresenter;
import org.obiba.opal.web.gwt.app.client.report.edit.ReportTemplateEditModalView;
import org.obiba.opal.web.gwt.app.client.report.list.ReportsPresenter;
import org.obiba.opal.web.gwt.app.client.report.list.ReportsView;
import org.obiba.opal.web.gwt.app.client.report.view.ReportTemplateDetailsPresenter;
import org.obiba.opal.web.gwt.app.client.report.view.ReportTemplateDetailsView;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class ReportsModule extends AbstractPresenterModule {

  @Override
  protected void configure() {
    bindPresenter(ReportsAdministrationPresenter.class, ReportsAdministrationPresenter.Display.class,
        ReportsAdministrationView.class, ReportsAdministrationPresenter.Proxy.class);
    bindPresenterWidget(ReportsPresenter.class, ReportsPresenter.Display.class, ReportsView.class);
    bindPresenterWidget(ReportTemplateDetailsPresenter.class, ReportTemplateDetailsPresenter.Display.class,
        ReportTemplateDetailsView.class);
    bindPresenterWidget(ReportTemplateEditModalPresenter.class, ReportTemplateEditModalPresenter.Display.class,
        ReportTemplateEditModalView.class);
  }
}
