package org.obiba.opal.web.gwt.app.client.administration.configuration.presenter;

import org.obiba.opal.web.gwt.app.client.administration.configuration.event.GeneralConfigSavedEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.keystore.presenter.CreateKeyPairModalPresenter;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.support.BreadcrumbsBuilder;
import org.obiba.opal.web.gwt.app.client.support.ErrorResponseCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.opal.GeneralConf;
import org.obiba.opal.web.model.client.opal.KeyForm;
import org.obiba.opal.web.model.client.opal.KeyType;
import org.obiba.opal.web.model.client.opal.PrivateKeyForm;
import org.obiba.opal.web.model.client.opal.PublicKeyForm;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import static com.google.gwt.http.client.Response.SC_BAD_REQUEST;
import static com.google.gwt.http.client.Response.SC_INTERNAL_SERVER_ERROR;
import static com.google.gwt.http.client.Response.SC_OK;

public class ConfigurationPresenter extends Presenter<ConfigurationPresenter.Display, ConfigurationPresenter.Proxy>
    implements ConfigurationUiHandlers {

  private final ModalProvider<GeneralConfModalPresenter> generalConfModalProvider;

  private final ModalProvider<CreateKeyPairModalPresenter> createKeyPairModalProvider;

  private GeneralConf conf = null;

  @ProxyStandard
  @NameToken(Places.SERVER)
  public interface Proxy extends ProxyPlace<ConfigurationPresenter> {}

  private static final Translations translations = GWT.create(Translations.class);

  private final BreadcrumbsBuilder breadcrumbsBuilder;

  @Inject
  public ConfigurationPresenter(Display display, EventBus eventBus, Proxy proxy,
      ModalProvider<GeneralConfModalPresenter> generalConfModalProvider,
      ModalProvider<CreateKeyPairModalPresenter> createKeyPairModalProvider, BreadcrumbsBuilder breadcrumbsBuilder) {
    super(eventBus, display, proxy, ApplicationPresenter.WORKBENCH);
    this.createKeyPairModalProvider = createKeyPairModalProvider.setContainer(this);
    this.generalConfModalProvider = generalConfModalProvider.setContainer(this);
    this.breadcrumbsBuilder = breadcrumbsBuilder;
    getView().setUiHandlers(this);
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
        .forResource(UriBuilders.SYSTEM_CONF_GENERAL.create().build())
        .withCallback(new ResourceCallback<GeneralConf>() {

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

  @Override
  public void onCreateKeyPair() {
    CreateKeyPairModalPresenter dialog = createKeyPairModalProvider.get();
    dialog.initialize(new CreateKeyPairModalPresenter.SaveHandler() {

      @SuppressWarnings("MethodWithTooManyParameters")
      @Override
      public void save(String algorithm, String size, String firstLastName, String organization,
          String organizationalUnit, String locality, String state, String country) {

        KeyForm keyForm = KeyForm.create();
        keyForm.setAlias("https");
        keyForm.setKeyType(KeyType.KEY_PAIR);

        keyForm.setPrivateForm(getPrivateKeyForm(algorithm, size));
        keyForm
            .setPublicForm(getPublicKeyForm(firstLastName, organization, organizationalUnit, locality, state, country));

        ResourceRequestBuilderFactory.newBuilder() //
            .forResource(UriBuilders.SYSTEM_KEYSTORE.create().build()) //
            .withResourceBody(KeyForm.stringify(keyForm)) //
            .withCallback(SC_OK, new ResponseCodeCallback() {
              @Override
              public void onResponseCode(Request request, Response response) {
                GWT.log("key pair CREATED ");
              }
            }) //
            .withCallback(new ErrorResponseCallback(getView().asWidget()), SC_BAD_REQUEST, SC_BAD_REQUEST,
                SC_INTERNAL_SERVER_ERROR) //
            .put().send();
      }

      private PrivateKeyForm getPrivateKeyForm(String algorithm, String size) {
        PrivateKeyForm privateForm = PrivateKeyForm.create();
        privateForm.setAlgo(algorithm);
        privateForm.setSize(Integer.parseInt(size));
        return privateForm;
      }

      private PublicKeyForm getPublicKeyForm(String firstLastName, String organization, String organizationalUnit,
          String locality, String state, String country) {
        PublicKeyForm publicForm = PublicKeyForm.create();
        publicForm.setName(firstLastName);
        publicForm.setOrganization(organization);
        publicForm.setOrganizationalUnit(organizationalUnit);
        publicForm.setLocality(locality);
        publicForm.setState(state);
        publicForm.setCountry(country);
        return publicForm;
      }
    });
  }

  @Override
  public void onDownloadCertificate() {
    getEventBus()
        .fireEvent(new FileDownloadRequestEvent(UriBuilders.SYSTEM_KEYSTORE_HTTPS_CERTIFICATE.create().build()));
  }

  public interface Display extends View, HasUiHandlers<ConfigurationUiHandlers>, HasBreadcrumbs {
    void renderGeneralProperties(GeneralConf resource);
  }
}
