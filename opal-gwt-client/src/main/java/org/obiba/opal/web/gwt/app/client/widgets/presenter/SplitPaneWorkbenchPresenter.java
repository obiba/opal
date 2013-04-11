/*******************************************************************************
 * Copyright 2012(c) OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.presenter;

import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;

import com.google.gwt.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

@SuppressWarnings("rawtypes")
public abstract class SplitPaneWorkbenchPresenter<D extends View, P extends Proxy<? extends SplitPaneWorkbenchPresenter>>
    extends Presenter<D, P> {

  public enum Slot {
    CENTER, LEFT
  }

  protected SplitPaneWorkbenchPresenter(EventBus eventBus, D display, P proxy) {
    super(eventBus, display, proxy);
  }

  @Override
  protected void revealInParent() {
    RevealContentEvent.fire(this, ApplicationPresenter.WORKBENCH, this);
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    authorize();
  }

  @Override
  protected void onBind() {
    super.onBind();
    addHandlers();
    for(Slot slot : Slot.values()) {
      setInSlot(slot, getDefaultPresenter(slot));
    }
  }

  protected abstract PresenterWidget<?> getDefaultPresenter(Slot slot);

  protected void authorize() {

  }

  protected void addHandlers() {
  }
}
