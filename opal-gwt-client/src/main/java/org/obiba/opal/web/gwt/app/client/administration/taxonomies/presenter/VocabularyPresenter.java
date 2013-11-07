/* Copyright 2013(c) OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.support.BreadcrumbsBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.opal.GeneralConf;
import org.obiba.opal.web.model.client.opal.TermDto;
import org.obiba.opal.web.model.client.opal.VocabularyDto;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import static org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter.VocabularyPresenter.Display;

public class VocabularyPresenter extends Presenter<Display, VocabularyPresenter.Proxy> implements VocabularyUiHandlers {

  private final PlaceManager placeManager;

  @ProxyStandard
  @NameToken(Places.VOCABULARY)
  public interface Proxy extends ProxyPlace<VocabularyPresenter> {}

  private final Translations translations;

  private final BreadcrumbsBuilder breadcrumbsBuilder;

  private String taxonomyName;

  private String vocabularyName;

  private String termName;

  @Inject
  public VocabularyPresenter(Display display, EventBus eventBus, Proxy proxy, PlaceManager placeManager,
      Translations translations, BreadcrumbsBuilder breadcrumbsBuilder) {
    super(eventBus, display, proxy, ApplicationPresenter.WORKBENCH);
    this.translations = translations;
    this.breadcrumbsBuilder = breadcrumbsBuilder;
    this.placeManager = placeManager;
    getView().setUiHandlers(this);
  }

  @Override
  public void prepareFromRequest(PlaceRequest request) {
    super.prepareFromRequest(request);
    taxonomyName = request.getParameter(TaxonomyTokens.TOKEN_TAXONOMY, null);
    vocabularyName = request.getParameter(TaxonomyTokens.TOKEN_VOCABULARY, null);
    termName = request.getParameter(TaxonomyTokens.TOKEN_TERM, null);

    ResourceRequestBuilderFactory.<GeneralConf>newBuilder()
        .forResource(UriBuilders.SYSTEM_CONF_GENERAL.create().build())
        .withCallback(new ResourceCallback<GeneralConf>() {
          @Override
          public void onResource(Response response, GeneralConf resource) {
            JsArrayString locales = JsArrayString.createArray().cast();
            for(int i = 0; i < resource.getLanguagesArray().length(); i++) {
              locales.push(resource.getLanguages(i));
            }
            getView().setAvailableLocales(locales);
            refresh();
          }
        }).get().send();
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    breadcrumbsBuilder.setBreadcrumbView(getView().getBreadcrumbs()).build();
  }

  private void refresh() {
    ResourceRequestBuilderFactory.<VocabularyDto>newBuilder()
        .forResource(UriBuilders.SYSTEM_CONF_TAXONOMY_VOCABULARY.create().build(taxonomyName, vocabularyName))//
        .get()//
        .withCallback(new ResourceCallback<VocabularyDto>() {

          @Override
          public void onResource(Response response, VocabularyDto resource) {
            getView().displayVocabulary(resource, taxonomyName, termName);

          }
        }).send();

  }

  @TitleFunction
  public String getTitle() {
    return translations.pageVocabularyTitle();
  }

  @Override
  public void onTermSelection(TermDto termDto) {
    getView().displayTerm(termDto);
  }

  @Override
  public void onEditVocabulary() {
    placeManager.revealRelativePlace(
        new PlaceRequest.Builder().nameToken(Places.VOCABULARY_EDIT).with(TaxonomyTokens.TOKEN_TAXONOMY, taxonomyName)
            .with(TaxonomyTokens.TOKEN_VOCABULARY, vocabularyName).build(), 2);
  }

  public interface Display extends View, HasBreadcrumbs, HasUiHandlers<VocabularyUiHandlers> {

    void displayVocabulary(VocabularyDto vocabulary, String taxonomy, String term);

    void displayTerm(TermDto termDto);

    void setAvailableLocales(JsArrayString locales);
  }
}
