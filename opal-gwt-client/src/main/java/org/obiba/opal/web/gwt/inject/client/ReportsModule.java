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

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;

public class ReportsModule extends AbstractGinModule {

  @Override
  protected void configure() {
    bind(ReportTemplateListPresenter.Display.class).to(ReportTemplateListView.class).in(Singleton.class);
    bind(ReportTemplateDetailsPresenter.Display.class).to(ReportTemplateDetailsView.class).in(Singleton.class);
    bind(ReportTemplatePresenter.Display.class).to(ReportTemplateView.class).in(Singleton.class);
    bind(ReportTemplateUpdateDialogPresenter.Display.class).to(ReportTemplateUpdateDialogView.class).in(Singleton.class);
  }

}
