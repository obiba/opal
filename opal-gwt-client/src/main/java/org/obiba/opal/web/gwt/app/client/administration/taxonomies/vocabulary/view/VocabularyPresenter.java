/* Copyright 2013(c) OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package org.obiba.opal.web.gwt.app.client.administration.taxonomies.vocabulary.view;

import org.obiba.opal.web.gwt.app.client.administration.taxonomies.event.TaxonomySelectedEvent;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.event.VocabularyDeletedEvent;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.event.VocabularyUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.term.edit.TermEditModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.vocabulary.edit.VocabularyEditModalPresenter;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.opal.LocaleTextDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.opal.TermDto;
import org.obiba.opal.web.model.client.opal.VocabularyDto;

import com.google.common.base.Strings;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import static com.google.gwt.http.client.Response.SC_NOT_FOUND;
import static com.google.gwt.http.client.Response.SC_OK;
import static org.obiba.opal.web.gwt.app.client.administration.taxonomies.vocabulary.view.VocabularyPresenter.Display;

public class VocabularyPresenter extends PresenterWidget<Display> implements VocabularyUiHandlers {

  private final Translations translations;

  private final TranslationMessages translationMessages;

  private final ModalProvider<VocabularyEditModalPresenter> vocabularyEditModalProvider;

  private final ModalProvider<TermEditModalPresenter> termEditModalProvider;

  private Runnable actionRequiringConfirmation;

  private TaxonomyDto taxonomy;

  private VocabularyDto vocabulary;

  @Inject
  public VocabularyPresenter(Display display, EventBus eventBus, Translations translations,
      TranslationMessages translationMessages, ModalProvider<VocabularyEditModalPresenter> vocabularyEditModalProvider,
      ModalProvider<TermEditModalPresenter> termEditModalProvider) {
    super(eventBus, display);
    this.translations = translations;
    this.translationMessages = translationMessages;
    this.vocabularyEditModalProvider = vocabularyEditModalProvider.setContainer(this);
    this.termEditModalProvider = termEditModalProvider.setContainer(this);
    getView().setUiHandlers(this);
    addHandlers();
  }

  public void setVocabulary(TaxonomyDto taxonomy, String name) {
    this.taxonomy = taxonomy;
    refresh(taxonomy.getName(), name);
  }

  private void refresh(final String taxonomyName, String name) {
    ResourceRequestBuilderFactory.<VocabularyDto>newBuilder().forResource(
        UriBuilders.SYSTEM_CONF_TAXONOMY_VOCABULARY.create().build(taxonomyName, name))//
        .get()//
        .withCallback(new ResourceCallback<VocabularyDto>() {

          @Override
          public void onResource(Response response, VocabularyDto resource) {
            vocabulary = resource;
            if(getVocabularyIndex() == -1) {
              // vocabulary could have been renamed
              ResourceRequestBuilderFactory.<TaxonomyDto>newBuilder() //
                  .forResource(UriBuilders.SYSTEM_CONF_TAXONOMY.create().build(taxonomyName)) //
                  .withCallback(new ResourceCallback<TaxonomyDto>() {
                    @Override
                    public void onResource(Response response, TaxonomyDto resource) {
                      taxonomy = resource;
                      getView().renderVocabulary(taxonomy, vocabulary);
                    }
                  }) //
                  .withCallback(SC_NOT_FOUND, new ResponseCodeCallback() {

                    @Override
                    public void onResponseCode(Request request, Response response) {
                      fireEvent(NotificationEvent.newBuilder().error("TaxonomyNotFound").args(taxonomyName).build());
                    }
                  }) //
                  .get().send();
            } else {
              getView().renderVocabulary(taxonomy, resource);
            }
          }
        }).withCallback(SC_NOT_FOUND, new VocabularyNotFoundCallback(taxonomy.getName(), name)) //
        .send();
  }

  @Override
  public void onTaxonomySelected() {
    fireEvent(new TaxonomySelectedEvent(taxonomy.getName()));
  }

  @Override
  public void onAddTerm() {
    termEditModalProvider.get().initView(taxonomy, vocabulary, TermDto.create());
  }

  @Override
  public void onEditTerm(TermDto termDto) {
    termEditModalProvider.get().initView(taxonomy, vocabulary, termDto);
  }

  @Override
  public void onDeleteTerm(final TermDto termDto) {
    actionRequiringConfirmation = new Runnable() {
      @Override
      public void run() {
        ResourceRequestBuilderFactory.newBuilder() //
            .forResource(UriBuilders.SYSTEM_CONF_TAXONOMY_VOCABULARY_TERM.create()
                .build(taxonomy.getName(), vocabulary.getName(), termDto.getName())) //
            .withCallback(SC_OK, new ResponseCodeCallback() {
              @Override
              public void onResponseCode(Request request, Response response) {
                fireEvent(new VocabularyUpdatedEvent(taxonomy.getName(), vocabulary.getName()));
              }
            }) //
            .withCallback(SC_NOT_FOUND,
                new TermNotFoundCallback(taxonomy.getName(), vocabulary.getName(), termDto.getName())) //
            .delete().send();
      }
    };
    fireEvent(ConfirmationRequiredEvent
        .createWithMessages(actionRequiringConfirmation, translationMessages.removeTerm(),
            translationMessages.confirmDeleteTerm()));
  }

  @Override
  public void onDelete() {
    actionRequiringConfirmation = new Runnable() {
      @Override
      public void run() {
        ResourceRequestBuilderFactory.newBuilder() //
            .forResource(
                UriBuilders.SYSTEM_CONF_TAXONOMY_VOCABULARY.create().build(taxonomy.getName(), vocabulary.getName())) //
            .withCallback(SC_OK, new ResponseCodeCallback() {
              @Override
              public void onResponseCode(Request request, Response response) {
                fireEvent(new VocabularyDeletedEvent(taxonomy.getName(), vocabulary.getName()));
              }
            }) //
            .withCallback(SC_NOT_FOUND, new VocabularyNotFoundCallback(taxonomy.getName(), vocabulary.getName())) //
            .delete().send();
      }
    };
    fireEvent(ConfirmationRequiredEvent
        .createWithMessages(actionRequiringConfirmation, translationMessages.removeVocabulary(),
            translationMessages.confirmDeleteVocabulary()));
  }

  @Override
  public void onPrevious() {
    int idx = getVocabularyIndex();
    if(idx > 0) {
      vocabulary = taxonomy.getVocabularies(idx - 1);
      getView().renderVocabulary(taxonomy, vocabulary);
    }
  }

  @Override
  public void onNext() {
    int idx = getVocabularyIndex();
    if(idx < taxonomy.getVocabulariesCount() - 1) {
      vocabulary = taxonomy.getVocabularies(idx + 1);
      getView().renderVocabulary(taxonomy, vocabulary);
    }
  }

  private int getVocabularyIndex() {
    for(int i = 0; i < taxonomy.getVocabulariesCount(); i++) {
      VocabularyDto current = taxonomy.getVocabularies(i);
      if(current.getName().equals(vocabulary.getName())) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public void onEdit() {
    vocabularyEditModalProvider.get().initView(taxonomy, vocabulary);
  }

  @Override
  public void onFilterUpdate(String filter) {
    if(Strings.isNullOrEmpty(filter)) {
      getView().renderTerms(vocabulary.getTermsArray());
    } else {
      JsArray<TermDto> filtered = JsArrays.create();
      for(TermDto term : JsArrays.toIterable(vocabulary.getTermsArray())) {
        if(termMatches(term, filter)) {
          filtered.push(term);
        }
      }
      getView().renderTerms(filtered);
    }
  }

  //
  // Private methods
  //

  private void addHandlers() {
    addRegisteredHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler());
    addRegisteredHandler(VocabularyUpdatedEvent.getType(), new VocabularyUpdatedEvent.VocabularyUpdatedHandler() {
      @Override
      public void onVocabularyUpdated(VocabularyUpdatedEvent event) {
        refresh(event.getTaxonomy(), event.getName());
      }
    });
  }

  private boolean termMatches(TermDto term, String filter) {
    String name = term.getName().toLowerCase();
    for(String token : filter.toLowerCase().split(" ")) {
      if(!Strings.isNullOrEmpty(token)) {
        if(!name.contains(token) && !textsContains(term.getTitleArray(), token) &&
            !textsContains(term.getDescriptionArray(), token)) return false;
      }
    }
    return true;
  }

  private boolean textsContains(JsArray<LocaleTextDto> texts, String token) {
    for(LocaleTextDto text : JsArrays.toIterable(texts)) {
      if(text.getText().contains(token)) return true;
    }
    return false;
  }

  public interface Display extends View, HasUiHandlers<VocabularyUiHandlers> {

    void renderVocabulary(TaxonomyDto taxonomy, VocabularyDto vocabulary);

    void renderTerms(JsArray<TermDto> terms);

  }

  private class ConfirmationEventHandler implements ConfirmationEvent.Handler {

    @Override
    public void onConfirmation(ConfirmationEvent event) {
      if(actionRequiringConfirmation != null && event.getSource().equals(actionRequiringConfirmation) &&
          event.isConfirmed()) {
        actionRequiringConfirmation.run();
        actionRequiringConfirmation = null;
      }
    }
  }

  private class VocabularyNotFoundCallback implements ResponseCodeCallback {

    private final String taxonomy;

    private final String vocabulary;

    private VocabularyNotFoundCallback(String taxonomy, String vocabulary) {
      this.taxonomy = taxonomy;
      this.vocabulary = vocabulary;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      fireEvent(NotificationEvent.newBuilder().error("VocabularyNotFound").args(taxonomy, vocabulary).build());
    }
  }

  private class TermNotFoundCallback implements ResponseCodeCallback {

    private final String taxonomy;

    private final String vocabulary;

    private final String term;

    private TermNotFoundCallback(String taxonomy, String vocabulary, String term) {
      this.taxonomy = taxonomy;
      this.vocabulary = vocabulary;
      this.term = term;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      fireEvent(NotificationEvent.newBuilder().error("TermNotFound").args(taxonomy, vocabulary, term).build());
    }
  }
}
