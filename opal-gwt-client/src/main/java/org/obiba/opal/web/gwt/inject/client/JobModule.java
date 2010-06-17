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

import org.obiba.opal.web.gwt.app.client.presenter.JobDetailsPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.JobListPresenter;
import org.obiba.opal.web.gwt.app.client.view.JobDetailsView;
import org.obiba.opal.web.gwt.app.client.view.JobListView;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;

/**
 *
 */
public class JobModule extends AbstractGinModule {

  @Override
  protected void configure() {
    // Bind concrete implementations to interfaces
    bind(JobListPresenter.Display.class).to(JobListView.class).in(Singleton.class);
    bind(JobDetailsPresenter.Display.class).to(JobDetailsView.class).in(Singleton.class);
  }

}
