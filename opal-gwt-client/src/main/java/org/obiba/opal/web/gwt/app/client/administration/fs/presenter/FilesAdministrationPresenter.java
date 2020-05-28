/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.fs.presenter;

import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.fs.FileDtos;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderRequestEvent;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileExplorerPresenter;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.support.BreadcrumbsBuilder;
import org.obiba.opal.web.gwt.rest.client.RequestCredentials;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class FilesAdministrationPresenter
    extends ItemAdministrationPresenter<FilesAdministrationPresenter.Display, FilesAdministrationPresenter.Proxy> {

  @ProxyStandard
  @NameToken(Places.FILES)
  public interface Proxy extends ProxyPlace<FilesAdministrationPresenter> {}

  private final RequestCredentials credentials;

  private final FileExplorerPresenter fileExplorerPresenter;

  private final BreadcrumbsBuilder breadcrumbsBuilder;

  @Inject
  public FilesAdministrationPresenter(Display display, EventBus eventBus, Proxy proxy, RequestCredentials credentials,
      FileExplorerPresenter fileExplorerPresenter, BreadcrumbsBuilder breadcrumbsBuilder) {
    super(eventBus, display, proxy);
    this.credentials = credentials;
    this.fileExplorerPresenter = fileExplorerPresenter;
    this.breadcrumbsBuilder = breadcrumbsBuilder;
  }

  @Override
  public void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event) {
  }

  @Override
  @TitleFunction
  public String getTitle() {
    return translations.pageFileExplorerTitle();
  }

  @Override
  public String getName() {
    return getTitle();
  }

  @Override
  public void authorize(HasAuthorization authorizer) {
  }

  @Override
  protected void onBind() {
    super.onBind();
    setInSlot("Explorer", fileExplorerPresenter);
  }

  @Override
  public void onReveal() {
    fireEvent(new FolderRequestEvent(FileDtos.user(credentials.getUsername())));
    breadcrumbsBuilder.setBreadcrumbView(getView().getBreadcrumbs()).build();
  }

  public interface Display extends View, HasBreadcrumbs {

  }
}
