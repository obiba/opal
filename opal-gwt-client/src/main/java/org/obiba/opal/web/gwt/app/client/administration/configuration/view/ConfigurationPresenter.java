/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.configuration.view;

import org.obiba.opal.web.gwt.app.client.administration.configuration.edit.GeneralConfModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.configuration.event.GeneralConfigSavedEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.keystore.presenter.CreateKeyPairModalPresenter;
import org.obiba.opal.web.gwt.app.client.keystore.presenter.ImportKeyPairModalPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.presenter.ResourcePermissionsPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.support.AclRequest;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionRequestPaths;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionType;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.support.BreadcrumbsBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.GeneralConf;

import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class ConfigurationPresenter extends Presenter<ConfigurationPresenter.Display, ConfigurationPresenter.Proxy>
    implements ConfigurationUiHandlers {

  private final ModalProvider<GeneralConfModalPresenter> generalConfModalProvider;

  private final ModalProvider<CreateKeyPairModalPresenter> createKeyPairModalProvider;

  private final ModalProvider<ImportKeyPairModalPresenter> importKeyPairModalProvider;

  private final Provider<ResourcePermissionsPresenter> resourcePermissionsProvider;

  private GeneralConf conf = null;

  @ProxyStandard
  @NameToken(Places.SERVER)
  public interface Proxy extends ProxyPlace<ConfigurationPresenter> {}

  private final Translations translations;

  private final BreadcrumbsBuilder breadcrumbsBuilder;

  @Inject
  public ConfigurationPresenter(Display display, EventBus eventBus, Proxy proxy, Translations translations,
      ModalProvider<GeneralConfModalPresenter> generalConfModalProvider,
      ModalProvider<CreateKeyPairModalPresenter> createKeyPairModalProvider,
      ModalProvider<ImportKeyPairModalPresenter> importKeyPairModalProvider,
      Provider<ResourcePermissionsPresenter> resourcePermissionsProvider, BreadcrumbsBuilder breadcrumbsBuilder) {
    super(eventBus, display, proxy, ApplicationPresenter.WORKBENCH);
    this.translations = translations;
    this.createKeyPairModalProvider = createKeyPairModalProvider.setContainer(this);
    this.importKeyPairModalProvider = importKeyPairModalProvider.setContainer(this);
    this.generalConfModalProvider = generalConfModalProvider.setContainer(this);
    this.resourcePermissionsProvider = resourcePermissionsProvider;
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
    // set permissions
    AclRequest.newResourceAuthorizationRequestBuilder()
        .authorize(new CompositeAuthorizer(getView().getPermissionsAuthorizer(), new PermissionsUpdate())).send();
  }

  @Override
  protected void onBind() {
    refresh();
    addRegisteredHandler(GeneralConfigSavedEvent.getType(), new GeneralConfigSavedEvent.GeneralConfigSavedHandler() {
      @Override
      public void onGeneralConfigSaved(GeneralConfigSavedEvent event) {
        refresh();
      }
    });
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
    createKeyPairModalProvider.get().initialize(null);
  }

  @Override
  public void onImportKeyPair() {
    importKeyPairModalProvider.get().initialize(ImportKeyPairModalPresenter.ImportType.KEY_PAIR, null);
  }

  @Override
  public void onDownloadCertificate() {
    fireEvent(new FileDownloadRequestEvent(UriBuilders.SYSTEM_KEYSTORE_HTTPS_CERTIFICATE.create().build()));
  }

  /**
   * Update permissions on authorization.
   */
  private final class PermissionsUpdate implements HasAuthorization {
    @Override
    public void unauthorized() {
      clearSlot(Display.Slots.Permissions);
    }

    @Override
    public void beforeAuthorization() {

    }

    @Override
    public void authorized() {
      ResourcePermissionsPresenter resourcePermissionsPresenter = resourcePermissionsProvider.get();
      resourcePermissionsPresenter.initialize(ResourcePermissionType.ADMINISTRATION,
          ResourcePermissionRequestPaths.UriBuilders.SYSTEM_PERMISSIONS_ADMINISTRATION);

      setInSlot(Display.Slots.Permissions, resourcePermissionsPresenter);
    }
  }

  public interface Display extends View, HasUiHandlers<ConfigurationUiHandlers>, HasBreadcrumbs {

    enum Slots {
      Permissions
    }

    void renderGeneralProperties(GeneralConf resource);

    HasAuthorization getPermissionsAuthorizer();
  }
}
