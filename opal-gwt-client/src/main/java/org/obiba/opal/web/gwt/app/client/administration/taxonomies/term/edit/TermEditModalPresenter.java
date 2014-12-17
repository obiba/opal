/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.taxonomies.term.edit;

import org.obiba.opal.web.gwt.app.client.administration.taxonomies.event.VocabularyUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
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
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public class TermEditModalPresenter extends ModalPresenterWidget<TermEditModalPresenter.Display>
    implements TermEditModalUiHandlers {

  private TaxonomyDto originalTaxonomy;

  private VocabularyDto originalVocabulary;

  private TermDto originalTerm;

  private EDIT_MODE mode;

  public enum EDIT_MODE {
    CREATE,
    EDIT
  }

  @Inject
  public TermEditModalPresenter(EventBus eventBus, Display display) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  @Override
  public void onSave(String name, JsArray<LocaleTextDto> titles, JsArray<LocaleTextDto> descriptions) {
    final TermDto dto = TermDto.create();
    dto.setName(name);
    dto.setTitleArray(titles);
    dto.setDescriptionArray(descriptions);

    if(mode == EDIT_MODE.EDIT) {
      ResourceRequestBuilderFactory.<TaxonomyDto>newBuilder().forResource(
          UriBuilders.SYSTEM_CONF_TAXONOMY_VOCABULARY_TERM.create()
              .build(originalTaxonomy.getName(), originalVocabulary.getName(), originalTerm.getName()))//
          .withResourceBody(TermDto.stringify(dto))//
          .withCallback(new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              getView().hide();
              getEventBus()
                  .fireEvent(new VocabularyUpdatedEvent(originalTaxonomy.getName(), originalVocabulary.getName()));
            }
          }, Response.SC_OK, Response.SC_CREATED)//
          .withCallback(new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              if(response.getText() != null && response.getText().length() != 0) {
                fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
              }
            }
          }, Response.SC_BAD_REQUEST, Response.SC_INTERNAL_SERVER_ERROR)//
          .put().send();
    } else {
      ResourceRequestBuilderFactory.<TaxonomyDto>newBuilder().forResource(
          UriBuilders.SYSTEM_CONF_TAXONOMY_VOCABULARY_TERMS.create()
              .build(originalTaxonomy.getName(), originalVocabulary.getName()))//
          .withResourceBody(TermDto.stringify(dto))//
          .withCallback(new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              getView().hide();
              getEventBus()
                  .fireEvent(new VocabularyUpdatedEvent(originalTaxonomy.getName(), originalVocabulary.getName()));
            }
          }, Response.SC_OK, Response.SC_CREATED)//
          .withCallback(new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              if(response.getText() != null && response.getText().length() != 0) {
                fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
              }
            }
          }, Response.SC_BAD_REQUEST, Response.SC_INTERNAL_SERVER_ERROR)//
          .post().send();
    }
  }

  public void initView(final TaxonomyDto taxonomyDto, final VocabularyDto vocabularyDto, final TermDto termDto) {
    originalTaxonomy = taxonomyDto;
    originalVocabulary = vocabularyDto;
    originalTerm = termDto;
    mode = termDto.hasName() ? EDIT_MODE.EDIT : EDIT_MODE.CREATE;

    ResourceRequestBuilderFactory.<GeneralConf>newBuilder()
        .forResource(UriBuilders.SYSTEM_CONF_GENERAL.create().build())
        .withCallback(new ResourceCallback<GeneralConf>() {
          @Override
          public void onResource(Response response, GeneralConf resource) {
            JsArrayString locales = JsArrayString.createArray().cast();
            for(int i = 0; i < resource.getLanguagesArray().length(); i++) {
              locales.push(resource.getLanguages(i));
            }
            getView().setMode(mode);
            getView().setTerm(termDto, locales);
          }
        }).get().send();
  }

  public interface Display extends PopupView, HasUiHandlers<TermEditModalUiHandlers> {

    enum FormField {
      VOCABULARY
    }

    void setMode(EDIT_MODE editionMode);

    void setTerm(TermDto term, JsArrayString locales);

    void showError(FormField formField, String message);
  }

}
