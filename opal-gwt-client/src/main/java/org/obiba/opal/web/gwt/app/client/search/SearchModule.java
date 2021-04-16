/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.search;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import org.obiba.opal.web.gwt.app.client.search.entities.SearchEntitiesPresenter;
import org.obiba.opal.web.gwt.app.client.search.entities.SearchEntitiesView;
import org.obiba.opal.web.gwt.app.client.search.entity.SearchEntityPresenter;
import org.obiba.opal.web.gwt.app.client.search.entity.SearchEntityView;
import org.obiba.opal.web.gwt.app.client.search.variables.SearchVariablesPresenter;
import org.obiba.opal.web.gwt.app.client.search.variables.SearchVariablesView;

/**
 *
 */
public class SearchModule extends AbstractPresenterModule {

  @Override
  protected void configure() {
    bindPresenter(SearchPresenter.class, SearchPresenter.Display.class, SearchView.class, SearchPresenter.Proxy.class);
    bindPresenter(SearchEntityPresenter.class, SearchEntityPresenter.Display.class, SearchEntityView.class, SearchEntityPresenter.Proxy.class);
    bindPresenter(SearchEntitiesPresenter.class, SearchEntitiesPresenter.Display.class, SearchEntitiesView.class, SearchEntitiesPresenter.Proxy.class);
    bindPresenter(SearchVariablesPresenter.class, SearchVariablesPresenter.Display.class, SearchVariablesView.class, SearchVariablesPresenter.Proxy.class);
  }
}
