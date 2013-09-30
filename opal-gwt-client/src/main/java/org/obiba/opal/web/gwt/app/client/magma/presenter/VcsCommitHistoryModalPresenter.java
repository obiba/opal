/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.presenter;

import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;
import org.obiba.opal.web.model.client.opal.VcsCommitInfoDto;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public class VcsCommitHistoryModalPresenter extends ModalPresenterWidget<VcsCommitHistoryModalPresenter.Display> {

  public interface Display extends PopupView, HasUiHandlers<ModalUiHandlers> {
    void setCommitInfo(VcsCommitInfoDto commitInfo);
  }

  @Inject
  public VcsCommitHistoryModalPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  public void setCommitInfo(VcsCommitInfoDto commitInfo) {
    getView().setCommitInfo(commitInfo);
  }

}
