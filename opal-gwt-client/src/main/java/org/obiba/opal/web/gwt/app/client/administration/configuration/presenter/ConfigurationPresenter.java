package org.obiba.opal.web.gwt.app.client.administration.configuration.presenter;

import org.obiba.opal.web.gwt.app.client.administration.configuration.event.GeneralConfigSavedEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.support.BreadcrumbsBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.opal.GeneralConf;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.http.client.Response;
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

  private final ModalProvider<GeneralConfModalPresenter> generalConfModalProvider;

  private GeneralConf conf = null;

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> CONTENT = new GwtEvent.Type<RevealContentHandler<?>>();

  @ProxyStandard
  @NameToken(Places.SERVER)
  public interface Proxy extends ProxyPlace<ConfigurationPresenter> {}

  private static final Translations translations = GWT.create(Translations.class);

  private final PlaceManager placeManager;

  private final BreadcrumbsBuilder breadcrumbsBuilder;

  @Inject
  public ConfigurationPresenter(Display display, EventBus eventBus, Proxy proxy,
      ModalProvider<GeneralConfModalPresenter> generalConfModalProvider, PlaceManager placeManager,
      BreadcrumbsBuilder breadcrumbsBuilder) {
    super(eventBus, display, proxy, ApplicationPresenter.WORKBENCH);
    this.generalConfModalProvider = generalConfModalProvider.setContainer(this);
    this.placeManager = placeManager;
    this.breadcrumbsBuilder = breadcrumbsBuilder;
    getView().setUiHandlers(this);
    setHistoryTokens();
  }

  @TitleFunction
  public String getTitle() {
    return translations.pageGeneralConfigurationTitle();
  }

  @Override
  public void onReveal() {
    super.onReveal();
    breadcrumbsBuilder.setBreadcrumbView(getView().getBreadcrumbs()).build();
  }

  @Override
  protected void onBind() {
    refresh();

    registerHandler(getEventBus()
        .addHandler(GeneralConfigSavedEvent.getType(), new GeneralConfigSavedEvent.GeneralConfigSavedHandler() {
          @Override
          public void onGeneralConfigSaved(GeneralConfigSavedEvent event) {
            refresh();
          }
        }));

  }

  private void refresh() {
    ResourceRequestBuilderFactory.<GeneralConf>newBuilder()//
        .forResource("/system/conf/general").withCallback(new ResourceCallback<GeneralConf>() {

      @Override
      public void onResource(Response response, GeneralConf resource) {
        conf = resource;
        getView().renderGeneralProperties(resource);
      }
    }).get().send();
  }

  @Override
  public void onEditGeneralSettings() {
    GeneralConfModalPresenter dialog = generalConfModalProvider.get();
    dialog.setGeneralConf(conf);
  }

  private void setHistoryTokens() {
    getView().setTaxonomiesHistoryToken(
        placeManager.buildRelativeHistoryToken(new PlaceRequest.Builder().nameToken(Places.TAXONOMIES).build(), 1));
  }

  public interface Display extends View, HasUiHandlers<ConfigurationUiHandlers>, HasBreadcrumbs {
    void setTaxonomiesHistoryToken(String historyToken);

    void renderGeneralProperties(GeneralConf resource);
  }
}
