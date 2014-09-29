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
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.opal.LocaleTextDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.opal.TermDto;
import org.obiba.opal.web.model.client.opal.VocabularyDto;

import com.google.common.base.Strings;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import static org.obiba.opal.web.gwt.app.client.administration.taxonomies.vocabulary.view.VocabularyPresenter.Display;

public class VocabularyPresenter extends PresenterWidget<Display> implements VocabularyUiHandlers {

  private final Translations translations;

  private TaxonomyDto taxonomy;

  private VocabularyDto vocabulary;

  @Inject
  public VocabularyPresenter(Display display, EventBus eventBus, Translations translations) {
    super(eventBus, display);
    this.translations = translations;
    getView().setUiHandlers(this);
  }

  public void setVocabulary(TaxonomyDto taxonomy, String name) {
    this.taxonomy = taxonomy;
    refresh(name);
  }

  private void refresh(String name) {
    ResourceRequestBuilderFactory.<VocabularyDto>newBuilder().forResource(
        UriBuilders.SYSTEM_CONF_TAXONOMY_VOCABULARY.create().build(taxonomy.getName(), name))//
        .get()//
        .withCallback(new ResourceCallback<VocabularyDto>() {

          @Override
          public void onResource(Response response, VocabularyDto resource) {
            vocabulary = resource;
            getView().renderVocabulary(taxonomy, resource);

          }
        }).send();
  }

  @Override
  public void onTaxonomySelected() {
    fireEvent(new TaxonomySelectedEvent(taxonomy.getName()));
  }

  @Override
  public void onEditTerm(TermDto termDto) {

  }

  @Override
  public void onDeleteTerm(TermDto termDto) {

  }

  @Override
  public void onDelete() {

  }

  @Override
  public void onEdit() {
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
}
