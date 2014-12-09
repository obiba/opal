/* Copyright 2013(c) OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package org.obiba.opal.web.gwt.app.client.administration.taxonomies.vocabulary.edit;

import org.obiba.opal.web.gwt.app.client.administration.taxonomies.TaxonomyTokens;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.vocabulary.TermArrayUtils;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.support.BreadcrumbsBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.opal.GeneralConf;
import org.obiba.opal.web.model.client.opal.LocaleTextDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.opal.TermDto;
import org.obiba.opal.web.model.client.opal.VocabularyDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.JsonUtils;
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
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import static org.obiba.opal.web.gwt.app.client.administration.taxonomies.vocabulary.edit.VocabularyEditPresenter.Display;

public class VocabularyEditPresenter extends Presenter<Display, VocabularyEditPresenter.Proxy>
    implements VocabularyEditUiHandlers {

  private TermDto currentTerm;

  @ProxyStandard
  @NameToken(Places.VOCABULARY_EDIT)
  public interface Proxy extends ProxyPlace<VocabularyEditPresenter> {}

  private final BreadcrumbsBuilder breadcrumbsBuilder;

  private final Translations translations;

  private final PlaceManager placeManager;

  private VocabularyDto vocabulary;

  private String taxonomyName;

  private String vocabularyName;

  @Inject
  public VocabularyEditPresenter(Display display, EventBus eventBus, Proxy proxy, BreadcrumbsBuilder breadcrumbsBuilder,
      Translations translations, PlaceManager placeManager) {
    super(eventBus, display, proxy, ApplicationPresenter.WORKBENCH);
    this.breadcrumbsBuilder = breadcrumbsBuilder;
    this.translations = translations;
    this.placeManager = placeManager;

    getView().setUiHandlers(this);
  }

  @Override
  public void prepareFromRequest(PlaceRequest request) {
    super.prepareFromRequest(request);
    taxonomyName = request.getParameter(TaxonomyTokens.TOKEN_TAXONOMY, null);
    vocabularyName = request.getParameter(TaxonomyTokens.TOKEN_VOCABULARY, null);

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

  @TitleFunction
  public String getTitle() {
    return translations.pageVocabularyTitle();
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    breadcrumbsBuilder.setBreadcrumbView(getView().getBreadcrumbs()).build();
  }

  private void refresh() {
    ResourceRequestBuilderFactory.<JsArray<TaxonomyDto>>newBuilder().forResource(
        UriBuilders.SYSTEM_CONF_TAXONOMIES.create().build())//
        .get().withCallback(new ResourceCallback<JsArray<TaxonomyDto>>() {
      @Override
      public void onResource(Response response, final JsArray<TaxonomyDto> taxonomies) {

        ResourceRequestBuilderFactory.<VocabularyDto>newBuilder().forResource(
            UriBuilders.SYSTEM_CONF_TAXONOMY_VOCABULARY.create().build(taxonomyName, vocabularyName))//
            .get()//
            .withCallback(new ResponseCodeCallback() {
              @Override
              public void onResponseCode(Request request, Response response) {
                vocabulary = JsonUtils.unsafeEval(response.getText());
                getView().getVocabularyName().setText(vocabulary.getName());
                getView().setTaxonomies(taxonomies);
                getView().setSelectedTaxonomy(taxonomyName);
                getView().getTitles().setValue(vocabulary.getTitleArray());
                getView().getDescriptions().setValue(vocabulary.getDescriptionArray());
                getView().getRepeatable().setValue(vocabulary.getRepeatable());

                getView().displayVocabulary(vocabulary);
              }
            }, Response.SC_OK)//
            .withCallback(new ResponseCodeCallback() {
              @Override
              public void onResponseCode(Request request, Response response) {
                vocabulary = null;
              }
            }, Response.SC_NOT_FOUND, Response.SC_INTERNAL_SERVER_ERROR).send();
      }
    }).get().send();
  }

  @Override
  public void onSave() {
    //Save current term
    saveCurrentTerm();

    VocabularyDto dto = VocabularyDto.create();
    dto.setName(getView().getVocabularyName().getText());
    dto.setTitleArray(getView().getTitles().getValue());
    dto.setDescriptionArray(getView().getDescriptions().getValue());
    dto.setRepeatable(getView().getRepeatable().getValue());
    dto.setTermsArray(JsArrays.toSafeArray(vocabulary.getTermsArray()));

    // Save vocabularyDto
    ResourceRequestBuilderFactory.newBuilder()//
        .forResource(UriBuilders.SYSTEM_CONF_TAXONOMY_VOCABULARY.create().build(taxonomyName, vocabularyName))
        .withResourceBody(VocabularyDto.stringify(dto)).accept("application/json")//
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            // nothing
          }
        }, Response.SC_OK)//
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            if(response.getText() != null && response.getText().length() != 0) {
              getEventBus().fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
            }
          }
        }, Response.SC_INTERNAL_SERVER_ERROR, Response.SC_BAD_REQUEST)//
        .put().send();

    placeManager.revealRelativePlace(new PlaceRequest.Builder().nameToken(Places.VOCABULARY)//
        .with(TaxonomyTokens.TOKEN_TAXONOMY, taxonomyName)//
        .with(TaxonomyTokens.TOKEN_VOCABULARY, vocabularyName)//
        .with(TaxonomyTokens.TOKEN_TERM, currentTerm != null ? currentTerm.getName() : "")//
        .build(), 2);

    currentTerm = null;
  }

  @Override
  public void onCancel() {
    placeManager.revealRelativePlace(
        new PlaceRequest.Builder().nameToken(Places.VOCABULARY).with(TaxonomyTokens.TOKEN_TAXONOMY, taxonomyName)
            .with(TaxonomyTokens.TOKEN_VOCABULARY, vocabularyName).build(), 2);
  }

  @Override
  public void onTermSelection(TermDto termDto) {
    saveCurrentTerm();

    currentTerm = termDto;

    getView().getTermName().setText(termDto.getName());
    getView().getTermTitles(termDto.getName()).setValue(termDto.getTitleArray());
    getView().getTermDescriptions(termDto.getName()).setValue(termDto.getDescriptionArray());
    getView().getTermPanel().setVisible(true);
  }

  private void saveCurrentTerm() {
    if(currentTerm != null) {
      // Save currentTerm titles, descriptions to map
      TermDto t = TermArrayUtils.findTerm(vocabulary.getTermsArray(), currentTerm.getName());
      t.setDescriptionArray(getView().getTermDescriptions(currentTerm.getName()).getValue());
      t.setTitleArray(getView().getTermTitles(currentTerm.getName()).getValue());
    }
  }

  @Override
  public void onAddChild(String text) {
    if(uniqueTermName(text)) {
      TermDto newTerm = TermDto.create();
      newTerm.setName(text);
      JsArray<TermDto> terms = JsArrays.create().cast();

      updateVocabularyWithChild(newTerm, terms);
      getView().displayVocabulary(vocabulary);
      getView().clearTermName();
      onTermSelection(newTerm);
    }
  }

  private void updateVocabularyWithChild(TermDto newTerm, JsArray<TermDto> terms) {
    if(currentTerm != null) {
      TermDto t = TermArrayUtils
          .findTerm(vocabulary.getTermsArray(), currentTerm != null ? currentTerm.getName() : null);

//      if(t.getTermsArray() == null) {
//        t.setTermsArray(terms);
//      }
//
//      t.getTermsArray().push(newTerm);
    } else {
      // Add at the end of terms
      for(int i = 0; i < vocabulary.getTermsCount(); i++) {
        terms.push(vocabulary.getTerms(i));
      }
      terms.push(newTerm);
      vocabulary.setTermsArray(terms);
    }
  }

  @Override
  public void onAddSibling(String text) {
    if(uniqueTermName(text)) {
      TermDto newTerm = TermDto.create();
      newTerm.setName(text);
      JsArray<TermDto> terms = JsArrays.create().cast();

      TermDto t = TermArrayUtils.findParent(null, vocabulary.getTermsArray(), currentTerm);

      updateVocabularyWithSibling(newTerm, terms, t);

      getView().displayVocabulary(vocabulary);
      getView().clearTermName();
      onTermSelection(newTerm);
    }
  }

  private void updateVocabularyWithSibling(TermDto newTerm, JsArray<TermDto> terms, TermDto t) {
    if(t != null) {
//      for(int i = 0; i < t.getTermsCount(); i++) {
//        terms.push(t.getTerms(i));
//
//        // Add after sibling
//        if(t.getTerms(i).getName().equals(currentTerm.getName())) {
//          terms.push(newTerm);
//        }
//      }
//
//      t.setTermsArray(terms);
    } else {
      for(int i = 0; i < vocabulary.getTermsCount(); i++) {
        terms.push(vocabulary.getTerms(i));
      }
      terms.push(newTerm);
      vocabulary.setTermsArray(terms);
    }
  }

  @Override
  public void onReorderTerms(String termName, int pos, boolean insertAfter) {
    // Modify vocabularyDto with the new structure
    TermDto term = TermArrayUtils.findTerm(vocabulary.getTermsArray(), termName);
    TermDto parent = TermArrayUtils.findParent(null, vocabulary.getTermsArray(), term);
    if(parent != null) {
      //parent.setTermsArray(getReorderedTerms(parent.getTermsArray(), term, pos, insertAfter));
    } else {
      // Reording at root level
      vocabulary.setTermsArray(getReorderedTerms(vocabulary.getTermsArray(), term, pos, insertAfter));
    }

    getView().displayVocabulary(vocabulary);
  }

  private JsArray<TermDto> getReorderedTerms(JsArray<TermDto> terms, TermDto term, int pos, boolean insertAfter) {
    int newPos = pos;

    JsArray<TermDto> termsArray = JsArrays.create().cast();
    for(int i = 0; i < terms.length(); i++) {
      if(!insertAfter && i == newPos) {
        termsArray.push(term);
      }

      if(!terms.get(i).getName().equals(term.getName())) {
        termsArray.push(terms.get(i));
      }

      if(insertAfter && i == newPos) {
        termsArray.push(term);
      }
    }

    return termsArray;
  }

  @Override
  public void onDeleteTerm(TermDto term) {
    // Modify vocabularyDto with the new structure
    TermDto parent = TermArrayUtils.findParent(null, vocabulary.getTermsArray(), term);
    JsArray<TermDto> termsArray = JsArrays.create().cast();
    for(int i = 0; i < vocabulary.getTermsCount(); i++) {
      if(!vocabulary.getTerms(i).getName().equals(term.getName())) {
        termsArray.push(vocabulary.getTerms(i));
      }
    }

    vocabulary.setTermsArray(termsArray);
    getView().displayVocabulary(vocabulary);
  }

  private boolean uniqueTermName(String name) {
    if(TermArrayUtils.findTerm(vocabulary.getTermsArray(), name) != null) {
      getEventBus().fireEvent(NotificationEvent.newBuilder().error("TermNameMustBeUnique").build());
      return false;
    }
    return true;
  }

  public interface Display extends View, HasBreadcrumbs, HasUiHandlers<VocabularyEditUiHandlers> {

    TakesValue<JsArray<LocaleTextDto>> getTitles();

    HasText getVocabularyName();

    TakesValue<JsArray<LocaleTextDto>> getDescriptions();

    void displayVocabulary(VocabularyDto vocabularyDto);

    HasValue<Boolean> getRepeatable();

    void setTaxonomies(JsArray<TaxonomyDto> taxonomies);

    void setSelectedTaxonomy(String taxonomyName);

    String getSelectedTaxonomy();

    HasText getTermName();

    TakesValue<JsArray<LocaleTextDto>> getTermTitles(String termName);

    TakesValue<JsArray<LocaleTextDto>> getTermDescriptions(String termName);

    Panel getTermPanel();

    void clearTermName();

    void setAvailableLocales(JsArrayString locales);
  }

}
