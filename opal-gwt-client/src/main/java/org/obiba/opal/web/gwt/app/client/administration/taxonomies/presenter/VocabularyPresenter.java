package org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter;

import org.obiba.opal.web.gwt.app.client.place.ParameterTokens;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.presenter.PageContainerPresenter;
import org.obiba.opal.web.gwt.app.client.support.BreadcrumbsBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto.VocabularyDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class VocabularyPresenter extends Presenter<VocabularyPresenter.Display, VocabularyPresenter.Proxy>
    implements VocabularyUiHandlers {

  @ProxyStandard
  @NameToken(Places.VOCABULARY)
  public interface Proxy extends ProxyPlace<VocabularyPresenter> {}

  public interface Display extends View, HasUiHandlers<VocabularyUiHandlers> {
    void setTaxonomyAndVocabulary(TaxonomyDto taxonomyDto, VocabularyDto vocabularyDto);

    HasWidgets getBreadcrumbs();
  }

  private final BreadcrumbsBuilder breadcrumbsBuilder;

  private final ModalProvider<AddVocabularyModalPresenter> addVocabularyModalProvider;

  private TaxonomyDto taxonomy;

  private JsArray<TaxonomyDto> taxonomies;

  private VocabularyDto vocabulary;

  private String taxonomyName;

  private String vocabularyName;

  @Inject
  public VocabularyPresenter(Display display, EventBus eventBus, Proxy proxy, BreadcrumbsBuilder breadcrumbsBuilder,
      ModalProvider<AddVocabularyModalPresenter> addVocabularyModalProvider) {
    super(eventBus, display, proxy, PageContainerPresenter.CONTENT);
    this.breadcrumbsBuilder = breadcrumbsBuilder;
    this.addVocabularyModalProvider = addVocabularyModalProvider.setContainer(this);
    getView().setUiHandlers(this);
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    breadcrumbsBuilder.setBreadcrumbView(getView().getBreadcrumbs()).build();
    refresh();
  }

  @Override
  public void prepareFromRequest(PlaceRequest request) {
    super.prepareFromRequest(request);
    String[] received = request.getParameter(ParameterTokens.TOKEN_NAME, null).split("/");
    taxonomyName = received[0];
    vocabularyName = received[1];
    refresh();
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
              }
            }
            getView().setTaxonomyAndVocabulary(taxonomy, vocabulary);
          }
        }).send();

  }

  @Override
  public void showEditVocabulary() {
    AddVocabularyModalPresenter presenter = addVocabularyModalProvider.get();
    presenter.setEditionMode(taxonomy, vocabulary);
  }

  @Override
  public void showAddTerm(TaxonomyDto taxonomyDto, VocabularyDto vocabularyDto) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void onTermSelection(TaxonomyDto taxonomyDto, VocabularyDto vocabularyDto, TaxonomyDto.TermDto termDto) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

}
