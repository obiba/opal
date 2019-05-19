/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.presenter;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.obiba.opal.web.gwt.app.client.event.ModalClosedEvent;

import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.PresenterWidget;

/**
 * Provider class for all modal dialogs extending {@link ModalPresenterWidget}.
 * Client code must set the modal container needed to manage modal slot.
 * <p/>
 * Usage:
 * <pre>
 * {@code
 *  &#064;Inject
 *  MyPresenter(ModalProvider<MyModal> modalProvider, ...) {
 *    this.modalProvider = modalProvider.setContainer(this);
 *  }
 *  public void showModal() {
 *    MyModal modal = modalProvider.get();
 *  }
 * }</pre>
 * The get() method adds the modal to its container triggering a show() on the modal. Once the modal is closed
 * the provider ModalCloseHandler does the cleanup and removes it from its container slot triggering a hide() on the
 * modal.
 */
public class ModalProvider<TPresenter extends ModalPresenterWidget<?>> implements Provider<TPresenter> {

  private final Provider<TPresenter> modalProvider;

  private final EventBus eventBus;

  private PresenterWidget<?> container;

  private TPresenter modal;

  private final Map<TPresenter, ModalListData<TPresenter>> modals
      = new HashMap<TPresenter, ModalListData<TPresenter>>();

  @Inject
  public ModalProvider(EventBus eventBus, Provider<TPresenter> modalProvider) {
    this.eventBus = eventBus;
    this.modalProvider = modalProvider;
  }

  public ModalProvider<TPresenter> setContainer(
      @SuppressWarnings("ParameterHidesMemberVariable") PresenterWidget<?> container) {
    this.container = container;
    return this;
  }

  /**
   * Create a new instance of modal without showing it.
   *
   * @return
   */
  public TPresenter create() {
    if(container == null) {
      throw new NullPointerException("Modal container is not set. Call ModalProvider.setContainer(PresenterWidget)");
    }
    return modal = modalProvider.get();
  }

  /**
   * Create a new instance of modal and show it.
   *
   * @return
   */
  @Override
  public TPresenter get() {
    create();
    return show();
  }

  /**
   * Create a instance of modal if it does not exist and show it.
   *
   * @return
   */
  public TPresenter show() {
    if(modal == null) create();
    HandlerRegistration closeHandler = eventBus.addHandler(ModalClosedEvent.getType(), new ModalClosedHandler());
    container.addToPopupSlot(modal);
    modals.put(modal, new ModalListData<TPresenter>(modal, closeHandler));
    return modal;
  }

  private class ModalClosedHandler implements ModalClosedEvent.Handler {

    @Override
    public void onModalClosed(ModalClosedEvent event) {
      Object source = event.getSource();
      ModalListData<TPresenter> modalListData = modals.get(source);

      if(modalListData != null) {
        TPresenter modalToClose = modalListData.getModal();
        modalListData.getCloseHandler().removeHandler();
        container.removeFromPopupSlot(modalToClose);
        modals.remove(modalToClose);
        if (modal == modalToClose) modal = null;
      }
    }
  }

  private class ModalListData<TModal> {
    private final TModal modal;

    private final HandlerRegistration closeHandler;

    private ModalListData(TModal modal, HandlerRegistration closeHandler) {
      this.modal = modal;
      this.closeHandler = closeHandler;
    }

    public TModal getModal() {
      return modal;
    }

    public HandlerRegistration getCloseHandler() {
      return closeHandler;
    }
  }

}
