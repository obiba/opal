/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.inject.client;

import org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplateDetailsPresenter;
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplateListPresenter;
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplatePresenter;
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplateUpdateDialogPresenter;
import org.obiba.opal.web.gwt.app.client.report.view.ReportTemplateDetailsView;
import org.obiba.opal.web.gwt.app.client.report.view.ReportTemplateListView;
import org.obiba.opal.web.gwt.app.client.report.view.ReportTemplateUpdateDialogView;
import org.obiba.opal.web.gwt.app.client.report.view.ReportTemplateView;

import com.google.inject.Singleton;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class ReportsModule extends AbstractPresenterModule {

  @Override
  protected void configure() {

    bindPresenter(ReportTemplatePresenter.class, ReportTemplatePresenter.Display.class, ReportTemplateView.class, ReportTemplatePresenter.Proxy.class);

    bind(ReportTemplateListPresenter.class).in(Singleton.class);
    bind(ReportTemplateListPresenter.Display.class).to(ReportTemplateListView.class).in(Singleton.class);
    bind(ReportTemplateDetailsPresenter.class).in(Singleton.class);
    bind(ReportTemplateDetailsPresenter.Display.class).to(ReportTemplateDetailsView.class).in(Singleton.class);
    bind(ReportTemplateUpdateDialogPresenter.Display.class).to(ReportTemplateUpdateDialogView.class);
  }

}
