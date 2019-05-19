/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.taxonomies.view;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Joiner;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.edit.TaxonomyEditModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.event.TaxonomyDeletedEvent;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.event.TaxonomyUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.event.VocabularySelectedEvent;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.vocabulary.edit.VocabularyEditModalPresenter;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationTerminatedEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.presenter.VcsCommitHistoryModalPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.support.FilterHelper;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.LocaleTextDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.opal.VcsCommitInfoDto;
import org.obiba.opal.web.model.client.opal.VcsCommitInfosDto;
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

public class TaxonomyPresenter extends PresenterWidget<TaxonomyPresenter.Display> implements TaxonomyUiHandlers {

  static final String HEAD_REVISION = "head";

  private Runnable actionRequiringConfirmation;

  private TaxonomyDto taxonomy;

  private boolean editable;

  private final TranslationMessages translationMessages;

  private final ModalProvider<TaxonomyEditModalPresenter> taxonomyEditModalProvider;

  private final ModalProvider<VocabularyEditModalPresenter> vocabularyEditModalProvider;

  private final ModalProvider<VcsCommitHistoryModalPresenter> vcsHistoryModalProvider;

  @Inject
  public TaxonomyPresenter(Display display, EventBus eventBus, TranslationMessages translationMessages,
      ModalProvider<TaxonomyEditModalPresenter> taxonomyEditModalProvider,
      ModalProvider<VocabularyEditModalPresenter> vocabularyEditModalProvider,
      ModalProvider<VcsCommitHistoryModalPresenter> vcsHistoryModalProvider) {
    super(eventBus, display);
    this.translationMessages = translationMessages;
    this.taxonomyEditModalProvider = taxonomyEditModalProvider.setContainer(this);
    this.vocabularyEditModalProvider = vocabularyEditModalProvider.setContainer(this);
    this.vcsHistoryModalProvider = vcsHistoryModalProvider.setContainer(this);
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
    super.onBind();
    initUiComponents();
    addHandlers();
    getView().getActions().setActionHandler(new TaxonomyCommitInfoActionHandler());
  }

  @Override
  protected void onUnbind() {
    super.onUnbind();
    taxonomy = null;
  }

  public void setTaxonomy(String name) {
    if(Strings.isNullOrEmpty(name)) {
      getView().setTaxonomy(null);
    } else {
      refreshTaxonomy(name);
    }
  }

  public void setEditable(boolean editable) {
    this.editable = editable;
    getView().setEditable(editable);
  }

  @Override
  public void onEdit() {
    taxonomyEditModalProvider.get().initView(taxonomy);
  }

  @Override
  public void onDownload() {
    String downloadUrl = UriBuilders.SYSTEM_CONF_TAXONOMY.create().build(taxonomy.getName()) + "/_download";
    fireEvent(new FileDownloadRequestEvent(downloadUrl));
  }

  @Override
  public void onDelete() {
    actionRequiringConfirmation = new Runnable() {
      @Override
      public void run() {
        String name = taxonomy.getName();
        ResourceRequestBuilderFactory.newBuilder() //
            .forResource(UriBuilders.SYSTEM_CONF_TAXONOMY.create().build(name)) //
            .withCallback(SC_OK, new ResponseCodeCallback() {
              @Override
              public void onResponseCode(Request request, Response response) {
                fireEvent(ConfirmationTerminatedEvent.create());
                fireEvent(new TaxonomyDeletedEvent(taxonomy));
              }
            }) //
            .withCallback(SC_NOT_FOUND, new TaxonomyNotFoundCallBack(name)) //
            .delete().send();
      }
    };
    fireEvent(ConfirmationRequiredEvent
        .createWithMessages(actionRequiringConfirmation, translationMessages.removeTaxonomy(),
            translationMessages.confirmDeleteTaxonomy()));
  }

  @Override
  public void onVocabularySelection(String vocabularyName) {
    fireEvent(new VocabularySelectedEvent(taxonomy, vocabularyName));
  }

  @Override
  public void onAddVocabulary() {
    vocabularyEditModalProvider.get()
        .initView(taxonomy, VocabularyDto.create());
  }

  @Override
  public void onSaveChanges() {
    saveTaxonomy();
  }

  @Override
  public void onResetChanges() {
    refreshTaxonomy(taxonomy.getName());
  }

  @Override
  public void onFilterUpdate(String filter) {
    if(Strings.isNullOrEmpty(filter)) {
      getView().setVocabularies(taxonomy.getVocabulariesArray());
    } else {
      List<String> tokens = FilterHelper.tokenize(filter);
      JsArray<VocabularyDto> filtered = JsArrays.create();
      for(VocabularyDto vocabulary : JsArrays.toIterable(taxonomy.getVocabulariesArray())) {
        if(vocabularyMatches(vocabulary, tokens)) filtered.push(vocabulary);
      }
      getView().setVocabularies(filtered);
    }
  }

  @Override
  public void onMoveUpVocabulary(VocabularyDto vocabularyDto) {
    List<VocabularyDto> vocabularies = JsArrays.toList(taxonomy.getVocabulariesArray());
    int idx = vocabularies.indexOf(vocabularyDto);

    if(idx > 0) {
      Collections.swap(vocabularies, idx, idx - 1);
      getView().setDirty(true);
      getView().setTaxonomy(taxonomy);
    }
  }

  @Override
  public void onMoveDownVocabulary(VocabularyDto vocabularyDto) {
    List<VocabularyDto> vocabularies = JsArrays.toList(taxonomy.getVocabulariesArray());
    int idx = vocabularies.indexOf(vocabularyDto);

    if(idx > -1 && idx < vocabularies.size() - 1) {
      Collections.swap(vocabularies, idx, idx + 1);
      getView().setDirty(true);
      getView().setTaxonomy(taxonomy);
    }
  }

  @Override
  public void onSortVocabularies(final boolean isAscending) {
    if(taxonomy.getVocabulariesCount() > 1) {
      Collections.sort(JsArrays.toList(taxonomy.getVocabulariesArray()), new Comparator<VocabularyDto>() {
        @Override
        public int compare(VocabularyDto o1, VocabularyDto o2) {
          return (isAscending ? 1 : -1) * o1.getName().compareTo(o2.getName());
        }
      });

      getView().setDirty(true);
      getView().setTaxonomy(taxonomy);
    }
  }

  private void saveTaxonomy() {
    UriBuilders.SYSTEM_CONF_TAXONOMY.create().build(taxonomy.getName());
    fireEvent(new TaxonomyUpdatedEvent(taxonomy.getName()));
    ResourceRequestBuilderFactory.<TaxonomyDto>newBuilder().forResource(
        UriBuilders.SYSTEM_CONF_TAXONOMY.create()
            .build(taxonomy.getName()))//
        .withResourceBody(TaxonomyDto.stringify(taxonomy))//
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getEventBus().fireEvent(new TaxonomyUpdatedEvent(taxonomy.getName()));
            getView().setDirty(false);
          }
        }, Response.SC_OK, Response.SC_CREATED)//
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            if(response.getText() != null && !response.getText().isEmpty()) {
              fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
            }
          }
        }, Response.SC_BAD_REQUEST, Response.SC_INTERNAL_SERVER_ERROR)//
        .put().send();
  }

  private boolean vocabularyMatches(VocabularyDto vocabulary, List<String> tokens) {
    String toText = Joiner.on(" ").join(vocabulary.getName(),
        Joiner.on(" ").join(JsArrays.toIterable(vocabulary.getTitleArray())),
        Joiner.on(" ").join(JsArrays.toIterable(vocabulary.getDescriptionArray())));
    return FilterHelper.matches(toText, tokens);
  }

  //
  // Private methods
  //

  @SuppressWarnings("unchecked")
  private void initUiComponents() {
    getView().setTaxonomy(null);
  }

  private void addHandlers() {
    addRegisteredHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler());
    addRegisteredHandler(TaxonomyDeletedEvent.getType(), new TaxonomyDeletedEvent.TaxonomyDeletedHandler() {
      @Override
      public void onTaxonomyDeleted(TaxonomyDeletedEvent event) {
        setTaxonomy(null);
      }
    });
  }

  private void authorize() {
    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.SYSTEM_CONF_TAXONOMY_COMMITS_INFO.create()
            .build(taxonomy.getName())).get()
        .authorize(new CompositeAuthorizer(getView().getCommitsAuthorizer(), new HasAuthorization() {
          @Override
          public void beforeAuthorization() {

          }

          @Override
          public void authorized() {
            retrieveCommitInfos();
          }

          @Override
          public void unauthorized() {

          }
        })).send();
  }

  private void refreshTaxonomy(String name) {
    ResourceRequestBuilderFactory.<TaxonomyDto>newBuilder() //
        .forResource(UriBuilders.SYSTEM_CONF_TAXONOMY.create().build(name)) //
        .withCallback(new TaxonomyFoundCallBack()) //
        .withCallback(SC_NOT_FOUND, new TaxonomyNotFoundCallBack(name)) //
        .get().send();
  }



  private class TaxonomyNotFoundCallBack implements ResponseCodeCallback {

    private final String taxonomyName;

    private TaxonomyNotFoundCallBack(String taxonomyName) {
      this.taxonomyName = taxonomyName;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      fireEvent(ConfirmationTerminatedEvent.create());
      fireEvent(NotificationEvent.newBuilder().error("TaxonomyNotFound").args(taxonomyName).build());
    }
  }

  private void retrieveCommitInfos() {
    String requestUri = UriBuilders.SYSTEM_CONF_TAXONOMY_COMMITS_INFO.create()
        .build(taxonomy.getName());

    ResourceRequestBuilderFactory.<VcsCommitInfosDto>newBuilder()//
        .forResource(requestUri).withCallback(new ResourceCallback<VcsCommitInfosDto>() {
      @Override
      public void onResource(Response response, VcsCommitInfosDto commitInfos) {
        getView().setData(commitInfos.getCommitInfosArray());
      }
    }).get().send();
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

  private class TaxonomyFoundCallBack implements ResourceCallback<TaxonomyDto> {

    @Override
    public void onResource(Response response, TaxonomyDto resource) {
      taxonomy = resource;
      getView().setTaxonomy(resource);
      getView().setDirty(false);
      authorize();
    }
  }

  public interface Display extends View, HasUiHandlers<TaxonomyUiHandlers> {
    String DIFF_ACTION = "CommitDiff";
    String DIFF_CURRENT_ACTION = "DiffWithCurrent";
    String RESTORE_ACTION = "Restore";

    void setData(JsArray<VcsCommitInfoDto> commitInfos);

    void setTaxonomy(@Nullable TaxonomyDto taxonomy);

    void setVocabularies(JsArray<VocabularyDto> vocabularies);

    void setEditable(boolean editable);

    void setDirty(boolean isDirty);

    HasAuthorization getCommitsAuthorizer();

    HasActionHandler<VcsCommitInfoDto> getActions();
  }

  private class TaxonomyCommitInfoActionHandler implements ActionHandler<VcsCommitInfoDto> {

    @Override
    public void doAction(VcsCommitInfoDto commitInfo, String actionName) {
      switch(actionName) {
        case Display.DIFF_ACTION:
          showCommitInfo(commitInfo, false);
          break;
        case Display.DIFF_CURRENT_ACTION:
          showCommitInfo(commitInfo, true);
          break;
        case Display.RESTORE_ACTION:
          restore(commitInfo);
          break;
      }
    }

    private void showCommitInfo(VcsCommitInfoDto dto, boolean withCurrent) {
      String requestUri = UriBuilders.SYSTEM_CONF_TAXONOMY_COMMIT_INFO.create()
          .build(taxonomy.getName(), withCurrent ? HEAD_REVISION : "", dto.getCommitId());

      ResourceRequestBuilderFactory.<VcsCommitInfoDto>newBuilder()//
          .forResource(requestUri).withCallback(new ResourceCallback<VcsCommitInfoDto>() {
        @Override
        public void onResource(Response response, VcsCommitInfoDto resource) {
          vcsHistoryModalProvider.get().setCommitInfo(resource);
        }
      }).get().send();
    }

    private void restore(VcsCommitInfoDto dto) {
      String requestUri = UriBuilders.SYSTEM_CONF_TAXONOMY_GIT_RESTORE.create()
          .build(taxonomy.getName(), dto.getCommitId());

      ResourceRequestBuilderFactory.<VcsCommitInfoDto>newBuilder()//
          .forResource(requestUri).withCallback(SC_OK, new ResponseCodeCallback() {
        @Override
        public void onResponseCode(Request request, Response response) {
          refreshTaxonomy(taxonomy.getName());
        }
      }).put().send();
    }
  }
}
