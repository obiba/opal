package org.obiba.opal.web.gwt.app.client.administration.configuration.presenter;

import javax.annotation.Nonnull;

import org.obiba.opal.web.gwt.app.client.administration.configuration.event.GeneralConfigSavedEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.keystore.presenter.CreateKeyPairModalPresenter;
import org.obiba.opal.web.gwt.app.client.keystore.presenter.ImportKeyPairModalPresenter;
import org.obiba.opal.web.gwt.app.client.keystore.presenter.commands.CreateKeyPairCommand;
import org.obiba.opal.web.gwt.app.client.keystore.presenter.commands.ImportKeyPairCommand;
import org.obiba.opal.web.gwt.app.client.keystore.presenter.commands.KeystoreCommand;
import org.obiba.opal.web.gwt.app.client.keystore.support.KeystoreType;
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
import org.obiba.opal.web.model.client.opal.KeyType;

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

import edu.umd.cs.findbugs.annotations.Nullable;

public class ConfigurationPresenter extends Presenter<ConfigurationPresenter.Display, ConfigurationPresenter.Proxy>
    implements ConfigurationUiHandlers {

  private static final String SYSTEM_ALIAS = "https";

  private final ModalProvider<GeneralConfModalPresenter> generalConfModalProvider;

  private final ModalProvider<CreateKeyPairModalPresenter> createKeyPairModalProvider;
  private final ModalProvider<ImportKeyPairModalPresenter> importKeyPairModalProvider;

  private GeneralConf conf = null;

  @ProxyStandard
  @NameToken(Places.SERVER)
  public interface Proxy extends ProxyPlace<ConfigurationPresenter> {}

  private static final Translations translations = GWT.create(Translations.class);

  private final BreadcrumbsBuilder breadcrumbsBuilder;

  @Inject
  public ConfigurationPresenter(Display display, EventBus eventBus, Proxy proxy,
      ModalProvider<GeneralConfModalPresenter> generalConfModalProvider,
      ModalProvider<CreateKeyPairModalPresenter> createKeyPairModalProvider,
      ModalProvider<ImportKeyPairModalPresenter> importKeyPairModalProvider, BreadcrumbsBuilder breadcrumbsBuilder) {
    super(eventBus, display, proxy, ApplicationPresenter.WORKBENCH);
    this.createKeyPairModalProvider = createKeyPairModalProvider.setContainer(this);
    this.importKeyPairModalProvider = importKeyPairModalProvider.setContainer(this);
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
    dialog.initialize(KeystoreType.SYSTEM, new CreateKeyPairModalPresenter.SaveHandler() {

      @SuppressWarnings("MethodWithTooManyParameters")
      @Override
      public void save(String alias, String algorithm, String size, String firstLastName, String organization,
          String organizationalUnit, String locality, String state, String country) {

        KeystoreCommand command = CreateKeyPairCommand.Builder.newBuilder()
            .setUrl(UriBuilders.SYSTEM_KEYSTORE.create().build())//
            .setAlias(SYSTEM_ALIAS)//
            .setAlgorithm(algorithm)//
            .setSize(size)//
            .setFirstLastName(firstLastName)//
            .setOrganization(organization)//
            .setOrganizationalUnit(organizationalUnit)//
            .setLocality(locality)//
            .setState(state)//
            .setCountry(country).build();

        command.execute(new SuccessCallback(), new ErrorResponseCallback(getView().asWidget()));
      }

      class SuccessCallback implements ResponseCodeCallback {

        @Override
        public void onResponseCode(Request request, Response response) {
          GWT.log("key pair CREATED ");
        }
      }
    });
  }


  @Override
  public void onImportKeyPair() {
    ImportKeyPairModalPresenter dialog = importKeyPairModalProvider.get();
    dialog.initialize(KeystoreType.SYSTEM, ImportKeyPairModalPresenter.ImportType.KEY_PAIR,
        new ImportKeyPairModalPresenter.SaveHandler() {
          @Override
          public void save(@Nonnull String publicKey, @Nullable String privateKey, @Nullable String alias) {
            KeystoreCommand command = ImportKeyPairCommand.Builder.newBuilder()
                .setUrl(UriBuilders.SYSTEM_KEYSTORE.create().build())//
                .setAlias(SYSTEM_ALIAS)//
                .setPublicKey(publicKey)//
                .setPrivateKey(privateKey)//
                .setKeyType(KeyType.KEY_PAIR)//
                .build();

            command.execute(new SuccessCallback(), new ErrorResponseCallback(getView().asWidget()));
          }

          class SuccessCallback implements ResponseCodeCallback {
            @Override
            public void onResponseCode(Request request, Response response) {
              GWT.log("key pair CREATED ");
            }
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
