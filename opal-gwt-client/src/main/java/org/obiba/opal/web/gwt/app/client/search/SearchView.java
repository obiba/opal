/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.search;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;
import org.obiba.opal.web.gwt.app.client.ui.OpalNavLink;

public class SearchView extends ViewImpl implements SearchPresenter.Display {

  interface Binder extends UiBinder<Widget, SearchView> {}

  @UiField
  OpalNavLink searchVariablesPlace;

  @UiField
  OpalNavLink searchEntitiesPlace;

  @UiField
  OpalNavLink searchEntityPlace;

  @Inject
  public SearchView(SearchView.Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void setVariablesHistoryToken(String historyToken) {
    searchVariablesPlace.setHistoryToken(historyToken);
  }

  @Override
  public void setEntitiesHistoryToken(String historyToken) {
    searchEntitiesPlace.setHistoryToken(historyToken);
  }

  @Override
  public void setEntityHistoryToken(String historyToken) {
    searchEntityPlace.setHistoryToken(historyToken);
  }

}
