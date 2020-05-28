/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.permissions.presenter;

import javax.annotation.Nonnull;

import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.model.client.opal.Subject;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public class DeleteAllConfirmationModalPresenter
    extends ModalPresenterWidget<DeleteAllConfirmationModalPresenter.Display>
    implements DeleteAllConfirmationModalUiHandlers {

  private Subject subject;

  private DeleteAllSubjectPermissionsHandler deleteAllHandler;

  @Inject
  public DeleteAllConfirmationModalPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  public void initialize(@Nonnull Subject subject, @Nonnull DeleteAllSubjectPermissionsHandler deleteAllHandler) {
    this.subject = subject;
    this.deleteAllHandler = deleteAllHandler;
    getView().setData(subject);
  }

  @Override
  public void deleteAll() {
    if (deleteAllHandler != null) {
      deleteAllHandler.deleteAllSubjectPermissions(subject);
    }
    getView().close();
  }

  public interface Display extends PopupView, HasUiHandlers<DeleteAllConfirmationModalUiHandlers> {
    void setData(Subject subject);
    void close();
  }
}
