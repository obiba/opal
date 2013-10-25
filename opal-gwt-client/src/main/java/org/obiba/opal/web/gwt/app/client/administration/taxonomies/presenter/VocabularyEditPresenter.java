/* Copyright 2013(c) OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.support.BreadcrumbsBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.LocaleTextDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.opal.TermDto;
import org.obiba.opal.web.model.client.opal.VocabularyDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Panel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import static org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter.VocabularyEditPresenter.Display;

public class VocabularyEditPresenter extends Presenter<Display, VocabularyEditPresenter.Proxy>
    implements VocabularyEditUiHandlers {

  private TermDto currentTerm;

  @ProxyStandard
  @NameToken(Places.VOCABULARY_EDIT)
  public interface Proxy extends ProxyPlace<VocabularyEditPresenter> {}

  private final BreadcrumbsBuilder breadcrumbsBuilder;

  private final PlaceManager placeManager;

  private TaxonomyDto taxonomy;

  private VocabularyDto vocabulary;

  private String taxonomyName;

  private String vocabularyName;

  @Inject
  public VocabularyEditPresenter(Display display, EventBus eventBus, Proxy proxy, BreadcrumbsBuilder breadcrumbsBuilder,
      PlaceManager placeManager) {
    super(eventBus, display, proxy, ApplicationPresenter.WORKBENCH);
    this.breadcrumbsBuilder = breadcrumbsBuilder;
    this.placeManager = placeManager;

    getView().setUiHandlers(this);
  }

  @Override
  public void prepareFromRequest(PlaceRequest request) {
    super.prepareFromRequest(request);
    taxonomyName = request.getParameter(TaxonomyTokens.TOKEN_TAXONOMY, null);
    vocabularyName = request.getParameter(TaxonomyTokens.TOKEN_VOCABULARY, null);

    refresh();
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    breadcrumbsBuilder.setBreadcrumbView(getView().getBreadcrumbs()).build();
  }

  private void refresh() {
    ResourceRequestBuilderFactory.<TaxonomyDto>newBuilder().forResource("/system/conf/taxonomy/" + taxonomyName).get()
        .withCallback(new ResourceCallback<TaxonomyDto>() {
          @Override
          public void onResource(Response response, TaxonomyDto resource) {
            taxonomy = resource;
            for(int i = 0; i < taxonomy.getVocabulariesCount(); i++) {
              if(taxonomy.getVocabularies(i).getName().equals(vocabularyName)) {
                vocabulary = taxonomy.getVocabularies(i);
                break;
              }
            }

            ResourceRequestBuilderFactory.<JsArray<TaxonomyDto>>newBuilder().forResource("/system/conf/taxonomies")
                .get().withCallback(new ResourceCallback<JsArray<TaxonomyDto>>() {
              @Override
              public void onResource(Response response, JsArray<TaxonomyDto> resource) {
                JsArray<TaxonomyDto> taxonomies = JsArrays.toSafeArray(resource);

                // TODO: Get locales from system config

                JsArrayString locales = JsArrayString.createArray().cast();
                locales.push("en");
                locales.push("fr");

                getView().getVocabularyName().setText(vocabulary.getName());
                getView().setTaxonomies(taxonomies);
                getView().setSelectedTaxonomy(taxonomyName);
                getView().getTitles(locales).setValue(vocabulary.getTitlesArray());
                getView().getDescriptions(locales).setValue(vocabulary.getDescriptionsArray());
                getView().getRepeatable().setValue(vocabulary.getRepeatable());

                getView().displayVocabulary(vocabulary);
              }
            }).send();
          }
        }).send();

  }

  @Override
  public void onSave() {
    JsArrayString locales = JsArrayString.createArray().cast();
    locales.push("en");
    locales.push("fr");

    vocabulary.setName(getView().getVocabularyName().getText());
    vocabulary.setTitlesArray(getView().getTitles(locales).getValue());
    vocabulary.setDescriptionsArray(getView().getDescriptions(locales).getValue());
    vocabulary.setTaxonomyName(getView().getSelectedTaxonomy());

    for(int i = 0; i < vocabulary.getTermsArray().length(); i++) {
      GWT.log("  - Term: " + vocabulary.getTerms(i).getName());
    }

    // Save vocabulary
    ResourceRequestBuilderFactory.newBuilder()//
        .forResource("/system/conf/taxonomy/" + taxonomyName + "/vocabulary/" + vocabularyName)
        .withResourceBody(VocabularyDto.stringify(vocabulary)).accept("application/json")//
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            GWT.log(response.getText());
          }
        }, Response.SC_OK, Response.SC_INTERNAL_SERVER_ERROR, Response.SC_BAD_REQUEST)//

        .put().send();

    PlaceRequest request = new PlaceRequest.Builder().nameToken(Places.VOCABULARY)
        .with(TaxonomyTokens.TOKEN_TAXONOMY, taxonomy.getName())
        .with(TaxonomyTokens.TOKEN_VOCABULARY, vocabulary.getName()).build();
    placeManager.revealPlace(request);
  }

  @Override
  public void onCancel() {
    PlaceRequest request = new PlaceRequest.Builder().nameToken(Places.VOCABULARY)
        .with(TaxonomyTokens.TOKEN_TAXONOMY, taxonomy.getName())
        .with(TaxonomyTokens.TOKEN_VOCABULARY, vocabulary.getName()).build();
    placeManager.revealPlace(request);
  }

  @Override
  public void onTermSelection(TermDto termDto) {
    currentTerm = termDto;

    // TODO: Get locales from system config
    JsArrayString locales = JsArrayString.createArray().cast();
    locales.push("en");
    locales.push("fr");

    getView().getTermName().setText(termDto.getName());
    getView().getTermTitles(locales).setValue(termDto.getTitlesArray());
    getView().getTermDescriptions(locales).setValue(termDto.getDescriptionsArray());
    getView().getTermPanel().setVisible(true);
  }

  @Override
  public void onAddChild(String text) {
    // TODO: Validate text is not empty or not...
    if(currentTerm != null) {
      TermDto t = TermArrayUtils.findTerm(vocabulary.getTermsArray(), currentTerm.getName());
      TermDto newTerm = TermDto.create();
      newTerm.setName(text);

      if(t.getTermsArray() == null) {
        t.setTermsArray(JsArrays.create().<JsArray<TermDto>>cast());
      }

      t.getTermsArray().push(newTerm);
      getView().displayVocabulary(vocabulary);
    }
  }

  @Override
  public void onAddSibling(String text) {
    if(currentTerm != null) {
      TermDto t = TermArrayUtils.findParent(null, vocabulary.getTermsArray(), currentTerm);
      TermDto newTerm = TermDto.create();
      newTerm.setName(text);

      JsArray<TermDto> terms = JsArrays.create().cast();
      for(int i = 0; i < t.getTermsCount(); i++) {
        terms.push(t.getTerms(i));

        // Add after sibling
        if(t.getTerms(i).getName().equals(currentTerm.getName())) {
          terms.push(newTerm);
        }
      }

      t.setTermsArray(terms);
      getView().displayVocabulary(vocabulary);
    }
  }

  public interface Display extends View, HasBreadcrumbs, HasUiHandlers<VocabularyEditUiHandlers> {

//    void displayTerm(TaxonomyDto.TermDto termDto);

    TakesValue<JsArray<LocaleTextDto>> getTitles(JsArrayString locales);

    HasText getVocabularyName();

    TakesValue<JsArray<LocaleTextDto>> getDescriptions(JsArrayString locales);

    void displayVocabulary(VocabularyDto vocabulary);

    HasValue<Boolean> getRepeatable();

    void setTaxonomies(JsArray<TaxonomyDto> taxonomies);

    void setSelectedTaxonomy(String taxonomyName);

    String getSelectedTaxonomy();

    HasText getTermName();

    TakesValue<JsArray<LocaleTextDto>> getTermTitles(JsArrayString locales);

    TakesValue<JsArray<LocaleTextDto>> getTermDescriptions(JsArrayString locales);

    Panel getTermPanel();
  }

  public static class TermArrayUtils {
    public static TermDto findTerm(JsArray<TermDto> terms, String termName) {
      // find in child
      for(int i = 0; i < terms.length(); i++) {
        if(terms.get(i).getName().equals(termName)) {
          return terms.get(i);
        }

        // Find in child
        TermDto t = findTerm(terms.get(i).getTermsArray(), termName);
        if(t != null) {
          return t;
        }
      }

      return null;
    }

    public static TermDto findParent(TermDto parent, JsArray<TermDto> terms, TermDto termToFind) {
      // find in child
      for(int i = 0; i < terms.length(); i++) {
        if(terms.get(i).getName().equals(termToFind.getName())) {
          return parent;
        }

        // Find in child
        TermDto t = findParent(terms.get(i), terms.get(i).getTermsArray(), termToFind);
        if(t != null) {
          return t;
        }
      }

      return null;
    }
  }

}
