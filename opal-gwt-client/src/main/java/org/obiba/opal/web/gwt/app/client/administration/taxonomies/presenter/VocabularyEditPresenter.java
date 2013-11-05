/* Copyright 2013(c) OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter;

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
import org.obiba.opal.web.model.client.opal.LocaleTextDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.opal.TermDto;
import org.obiba.opal.web.model.client.opal.VocabularyDto;

import com.google.gwt.core.client.GWT;
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
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import static org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter.VocabularyEditPresenter.Display;

public class VocabularyEditPresenter extends Presenter<Display, VocabularyEditPresenter.Proxy>
    implements VocabularyEditUiHandlers {

  private TermDto currentTerm;

  private JsArrayString locales;

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

    refresh();
  }

  @TitleFunction
  public String getTitle() {
    return translations.pageVocabularyTitle();
  }

  @Override
  protected void onBind() {
    super.onBind();
    locales = JsArrayString.createArray().cast();
    locales.push("en");
    locales.push("fr");
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    breadcrumbsBuilder.setBreadcrumbView(getView().getBreadcrumbs()).build();
  }

  private void refresh() {
    ResourceRequestBuilderFactory.<JsArray<TaxonomyDto>>newBuilder().forResource("/system/conf/taxonomies").get()
        .withCallback(new ResourceCallback<JsArray<TaxonomyDto>>() {
          @Override
          public void onResource(Response response, final JsArray<TaxonomyDto> taxonomies) {

            ResourceRequestBuilderFactory.<VocabularyDto>newBuilder()
                .forResource("/system/conf/taxonomy/" + taxonomyName + "/vocabularyDto/" + vocabularyName).get()
                .withCallback(new ResponseCodeCallback() {
                  @Override
                  public void onResponseCode(Request request, Response response) {
                    if(response.getStatusCode() == Response.SC_OK) {

                      vocabulary = JsonUtils.unsafeEval(response.getText());
                      getView().getVocabularyName().setText(vocabulary.getName());
                      getView().setTaxonomies(taxonomies);
                      getView().setSelectedTaxonomy(taxonomyName);
                      getView().getTitles(locales).setValue(vocabulary.getTitlesArray());
                      getView().getDescriptions(locales).setValue(vocabulary.getDescriptionsArray());
                      getView().getRepeatable().setValue(vocabulary.getRepeatable());

                      getView().displayVocabulary(vocabulary);

                    }
                    //TODO: Display error
                  }
                }, Response.SC_OK, Response.SC_NOT_FOUND, Response.SC_INTERNAL_SERVER_ERROR).send();
          }
        }).get().send();
  }

  @Override
  public void onSave() {
    //Save current term
    saveCurrentTerm();
    currentTerm = null;

    VocabularyDto dto = VocabularyDto.create();
    dto.setName(getView().getVocabularyName().getText());
    dto.setTitlesArray(getView().getTitles(locales).getValue());
    dto.setDescriptionsArray(getView().getDescriptions(locales).getValue());
    dto.setTaxonomyName(getView().getSelectedTaxonomy());
    dto.setRepeatable(getView().getRepeatable().getValue());
    dto.setTermsArray(vocabulary.getTermsArray());

    // Save vocabularyDto
    ResourceRequestBuilderFactory.newBuilder()//
        .forResource("/system/conf/taxonomy/" + taxonomyName + "/vocabulary/" + vocabularyName)
        .withResourceBody(VocabularyDto.stringify(dto)).accept("application/json")//
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            GWT.log(response.getText());
          }
        }, Response.SC_OK, Response.SC_INTERNAL_SERVER_ERROR, Response.SC_BAD_REQUEST)//
        .put().send();

    PlaceRequest request = new PlaceRequest.Builder().nameToken(Places.VOCABULARY)
        .with(TaxonomyTokens.TOKEN_TAXONOMY, taxonomyName).with(TaxonomyTokens.TOKEN_VOCABULARY, vocabularyName)
        .build();
    placeManager.revealPlace(request);
  }

  @Override
  public void onCancel() {
    PlaceRequest request = new PlaceRequest.Builder().nameToken(Places.VOCABULARY)
        .with(TaxonomyTokens.TOKEN_TAXONOMY, taxonomyName).with(TaxonomyTokens.TOKEN_VOCABULARY, vocabularyName)
        .build();
    placeManager.revealPlace(request);
  }

  @Override
  public void onTermSelection(TermDto termDto) {
    saveCurrentTerm();

    currentTerm = termDto;

    getView().getTermName().setText(termDto.getName());
    getView().getTermTitles(termDto.getName(), locales).setValue(termDto.getTitlesArray());
    getView().getTermDescriptions(termDto.getName(), locales).setValue(termDto.getDescriptionsArray());
    getView().getTermPanel().setVisible(true);
  }

  private void saveCurrentTerm() {
    // Save currentTerm titles, descriptions to map
    TermDto t = TermArrayUtils.findTerm(vocabulary.getTermsArray(), currentTerm.getName());
    t.setDescriptionsArray(getView().getTermDescriptions(currentTerm.getName(), locales).getValue());
    t.setTitlesArray(getView().getTermTitles(currentTerm.getName(), locales).getValue());
  }

  @Override
  public void onAddChild(String text) {
    if(uniqueTermName(text)) {
      if(currentTerm != null) {
        TermDto t = TermArrayUtils.findTerm(vocabulary.getTermsArray(), currentTerm.getName());
        TermDto newTerm = TermDto.create();
        newTerm.setName(text);

        if(t.getTermsArray() == null) {
          t.setTermsArray(JsArrays.create().<JsArray<TermDto>>cast());
        }

        t.getTermsArray().push(newTerm);
        getView().displayVocabulary(vocabulary);
        getView().clearTermName();
        onTermSelection(newTerm);
      }
    }
  }

  @Override
  public void onAddSibling(String text) {
    if(uniqueTermName(text)) {

      TermDto newTerm = TermDto.create();
      newTerm.setName(text);
      JsArray<TermDto> terms = JsArrays.create().cast();

      TermDto t = TermArrayUtils.findParent(null, vocabulary.getTermsArray(), currentTerm);

      if(t != null) {
        for(int i = 0; i < t.getTermsCount(); i++) {
          terms.push(t.getTerms(i));

          // Add after sibling
          if(t.getTerms(i).getName().equals(currentTerm.getName())) {
            terms.push(newTerm);
          }
        }

        t.setTermsArray(terms);
      } else {
        for(int i = 0; i < vocabulary.getTermsCount(); i++) {
          terms.push(vocabulary.getTerms(i));
        }
        terms.push(newTerm);
      }

      vocabulary.setTermsArray(terms);
      getView().displayVocabulary(vocabulary);
      getView().clearTermName();
      onTermSelection(newTerm);
    }
  }

  @Override
  public void onReorderTerms(String termName, int pos, boolean insertAfter) {
    // Modify vocabularyDto with the new structure
    TermDto term = TermArrayUtils.findTerm(vocabulary.getTermsArray(), termName);
    TermDto parent = TermArrayUtils.findParent(null, vocabulary.getTermsArray(), term);
    if(parent != null) {
      parent.setTermsArray(getReorderedTerms(parent.getTermsArray(), term, pos, insertAfter));
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

      // When a term has child terms, a FlowPanel was created for those terms
      if(vocabulary.getTerms(i).getTermsCount() > 0) newPos--;

      if(!insertAfter) {
        if(i == newPos) {
          termsArray.push(term);
        }
      }

      if(!vocabulary.getTerms(i).getName().equals(term.getName())) {
        termsArray.push(vocabulary.getTerms(i));
      }

      if(insertAfter) {
        if(i == newPos) {
          termsArray.push(term);
        }
      }
    }

    return termsArray;
  }

  private boolean uniqueTermName(String name) {
    if(TermArrayUtils.findTerm(vocabulary.getTermsArray(), name) != null) {
      getEventBus().fireEvent(NotificationEvent.newBuilder().error("TermNameMustBeUnique").build());
      return false;
    }
    return true;
  }

  public interface Display extends View, HasBreadcrumbs, HasUiHandlers<VocabularyEditUiHandlers> {

    TakesValue<JsArray<LocaleTextDto>> getTitles(JsArrayString localeTitles);

    HasText getVocabularyName();

    TakesValue<JsArray<LocaleTextDto>> getDescriptions(JsArrayString localeDescriptions);

    void displayVocabulary(VocabularyDto vocabularyDto);

    HasValue<Boolean> getRepeatable();

    void setTaxonomies(JsArray<TaxonomyDto> taxonomies);

    void setSelectedTaxonomy(String taxonomyName);

    String getSelectedTaxonomy();

    HasText getTermName();

    TakesValue<JsArray<LocaleTextDto>> getTermTitles(String termName, JsArrayString localeTitles);

    TakesValue<JsArray<LocaleTextDto>> getTermDescriptions(String termName, JsArrayString localeDescriptions);

    Panel getTermPanel();

    void clearTermName();
  }

  public static class TermArrayUtils {
    private TermArrayUtils() {}

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
