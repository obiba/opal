/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.keystore;

import org.obiba.opal.web.gwt.app.client.keystore.EncryptionKeysPresenter;
import org.obiba.opal.web.model.client.opal.ProjectDto;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class ProjectKeyStorePresenter extends PresenterWidget<ProjectKeyStorePresenter.Display>
    implements ProjectKeyStoreUiHandlers {

  private static final String ENCRYPTION_KEYS_SLOT = "encryption_keys";

  // TODO use a provider once encryption key authorization is implemented
  private final EncryptionKeysPresenter encryptionKeysPresenter;

  @Inject
  public ProjectKeyStorePresenter(Display display, EventBus eventBus,
      Provider<EncryptionKeysPresenter> encryptionKeysProvider) {
    super(eventBus, display);
    this.encryptionKeysPresenter = encryptionKeysProvider.get();
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
    super.onBind();
    setInSlot(ENCRYPTION_KEYS_SLOT, encryptionKeysPresenter);
  }

  public void initialize(ProjectDto projectDto) {
    encryptionKeysPresenter.initialize(projectDto);
  }

  public interface Display extends View, HasUiHandlers<ProjectKeyStoreUiHandlers> {}
}
