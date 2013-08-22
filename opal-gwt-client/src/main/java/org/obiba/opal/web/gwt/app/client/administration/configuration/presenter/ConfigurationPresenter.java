package org.obiba.opal.web.gwt.app.client.administration.configuration.presenter;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.presenter.PageContainerPresenter;
import org.obiba.opal.web.gwt.app.client.support.BreadcrumbsBuilder;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.GwtEvent;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

public class ConfigurationPresenter extends Presenter<ConfigurationPresenter.Display, ConfigurationPresenter.Proxy>
    implements ConfigurationUiHandlers {

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> CONTENT = new GwtEvent.Type<RevealContentHandler<?>>();

  @ProxyStandard
  @NameToken(Places.configuration)
  public interface Proxy extends ProxyPlace<ConfigurationPresenter> {}

  public interface Display extends View, HasUiHandlers<ConfigurationUiHandlers>, HasBreadcrumbs {
    void setTaxonomiesHistoryToken(String historyToken);
  }

  private static final Translations translations = GWT.create(Translations.class);

  private final PlaceManager placeManager;

  private final BreadcrumbsBuilder breadcrumbsBuilder;

  @Inject
  public ConfigurationPresenter(Display display, EventBus eventBus, Proxy proxy, PlaceManager placeManager,
      BreadcrumbsBuilder breadcrumbsBuilder) {
    super(eventBus, display, proxy, PageContainerPresenter.CONTENT);
    this.placeManager = placeManager;
    this.breadcrumbsBuilder = breadcrumbsBuilder;
    getView().setUiHandlers(this);
    setHistoryTokens();
  }

  @TitleFunction
  public String getTitle() {
    return translations.pageConfigurationTitle();
  }

  @Override
  public void onReveal() {
    super.onReveal();
    breadcrumbsBuilder.setBreadcrumbView(getView().getBreadcrumbs()).build();
  }

  @Override
  public void prepareFromRequest(PlaceRequest placeRequest) {

  }

  private void setHistoryTokens() {
    getView().setTaxonomiesHistoryToken(
        placeManager.buildRelativeHistoryToken(new PlaceRequest.Builder().nameToken(Places.taxonomies).build(), 2));
  }
}
