package org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter;

import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.PageContainerPresenter;
import org.obiba.opal.web.gwt.app.client.support.BreadcrumbsBuilder;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto.VocabularyDto;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class VocabularyPresenter extends Presenter<VocabularyPresenter.Display, VocabularyPresenter.Proxy>
    implements VocabularyUiHandlers {

  @ProxyStandard
  @NameToken(Places.taxonomy)
  public interface Proxy extends ProxyPlace<VocabularyPresenter> {}

  public interface Display extends View, HasUiHandlers<VocabularyUiHandlers> {
    void setTaxonomyAndVocabulary(TaxonomyDto taxonomyDto, VocabularyDto vocabularyDto);

    HasWidgets getBreadcrumbs();
  }

  private final BreadcrumbsBuilder breadcrumbsBuilder;

  @Inject
  public VocabularyPresenter(Display display, EventBus eventBus, Proxy proxy, BreadcrumbsBuilder breadcrumbsBuilder) {
    super(eventBus, display, proxy, PageContainerPresenter.CONTENT);
    this.breadcrumbsBuilder = breadcrumbsBuilder;
    getView().setUiHandlers(this);
  }

  @Override
  public void showEditVocabulary() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void showAddTerm(TaxonomyDto taxonomyDto, VocabularyDto vocabulary) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void onTermSelection(TaxonomyDto taxonomyDto, VocabularyDto vocabulary, TaxonomyDto.TermDto termDto) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

}
