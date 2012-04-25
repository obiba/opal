/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.inject;

import org.obiba.opal.web.gwt.app.client.job.presenter.JobDetailsPresenter;
import org.obiba.opal.web.gwt.app.client.job.presenter.JobListPresenter;
import org.obiba.opal.web.gwt.app.client.job.view.JobDetailsView;
import org.obiba.opal.web.gwt.app.client.job.view.JobListView;

import com.google.inject.Singleton;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class JobModule extends AbstractPresenterModule {

  @Override
  protected void configure() {
    // Bind concrete implementations to interfaces
    bindPresenter(JobListPresenter.class, JobListPresenter.Display.class, JobListView.class, JobListPresenter.Proxy.class);
    bind(JobDetailsPresenter.Display.class).to(JobDetailsView.class).in(Singleton.class);
  }

}
