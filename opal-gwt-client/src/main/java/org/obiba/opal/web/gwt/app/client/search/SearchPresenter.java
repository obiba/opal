/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.search;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.shared.proxy.TokenFormatter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.HasPageTitle;

import java.util.Arrays;

public class SearchPresenter extends Presenter<SearchPresenter.Display, SearchPresenter.Proxy> implements HasPageTitle {

  @ProxyStandard
  @NameToken(Places.SEARCH)
  public interface Proxy extends ProxyPlace<SearchPresenter> {}

  private final Translations translations;

  @Inject
  public SearchPresenter(SearchPresenter.Display display, EventBus eventBus, SearchPresenter.Proxy proxy, Translations translations,
                                 TokenFormatter tokenFormatter) {
    super(eventBus, display, proxy, ApplicationPresenter.WORKBENCH);
    this.translations = translations;
    setHistoryTokens(tokenFormatter);
  }

  @Override
  @TitleFunction
  public String getTitle() {
    return translations.pageSearchTitle();
  }

  private PlaceRequest createRequest(String nameToken) {
    return new PlaceRequest.Builder().nameToken(nameToken).build();
  }

  private void setHistoryTokens(TokenFormatter tokenFormatter) {
    PlaceRequest searchPlace = createRequest(Places.SEARCH);
    getView().setVariablesHistoryToken(getHistoryToken(tokenFormatter, searchPlace, Places.SEARCH_VARIABLES));
    getView().setEntitiesHistoryToken(getHistoryToken(tokenFormatter, searchPlace, Places.SEARCH_ENTITIES));
    getView().setEntityHistoryToken(getHistoryToken(tokenFormatter, searchPlace, Places.SEARCH_ENTITY));
  }

  private String getHistoryToken(TokenFormatter tokenFormatter, PlaceRequest placeRequest, String place) {
    return tokenFormatter.toHistoryToken(Arrays.asList(placeRequest, createRequest(place)));
  }

  public interface Display extends View {

    void setVariablesHistoryToken(String historyToken);

    void setEntitiesHistoryToken(String historyToken);

    void setEntityHistoryToken(String historyToken);
  }
}
