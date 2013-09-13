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
 *
 * @Inject MyPresenter(ModalProvider<MyModal> modalProvider, ...) {
 * this.modalProvider = modalProvider.setContainer(this);
 * }
 * <p/>
 * public void showModal() {
 * MyModal modal = modalProvider.get();
 * }
 * <p/>
 * The get() method adds the modal to its container triggering a show() on the modal. Once the modal is closed
 * the provider ModalCloseHandler does the cleanup and removes it from its container slot triggering a hide() on the
 * modal.
 */
public class ModalProvider<M extends ModalPresenterWidget> implements Provider<M> {

  private final Provider<M> modalProvider;

  private final EventBus eventBus;

  private PresenterWidget container;

  private M modal;

  private final Map<M, ModalListData<M>> modals = new HashMap<M, ModalListData<M>>();

  @Inject
  public ModalProvider(EventBus eventBus, Provider<M> modalProvider) {
    this.eventBus = eventBus;
    this.modalProvider = modalProvider;
  }

  public ModalProvider<M> setContainer(PresenterWidget container) {
    this.container = container;
    return this;
  }

  /**
   * Create a new instance of modal without showing it.
   * @return
   */
  public M create() {
    if(container == null) {
      throw new NullPointerException("Modal container is not set. Call ModalProvider.setContainer(PresenterWidget)");
    }
    return modal = modalProvider.get();
  }

  /**
   * Create a new instance of modal and show it.
   * @return
   */
  public M get() {
    create();
    return show();
  }

  /**
   * Ceate a instance of modal if it does not exist and show it.
   * @return
   */
  public M show() {
    if (modal == null) create();
    HandlerRegistration closeHandler = eventBus.addHandler(ModalClosedEvent.getType(), new ModalClosedHandler());
    container.addToPopupSlot(modal);
    modals.put(modal, new ModalListData<M>(modal, closeHandler));
    return modal;
  }

  private class ModalClosedHandler implements ModalClosedEvent.Handler {

    @Override
    public void onModalClosed(ModalClosedEvent event) {
      ModalListData<M> modalListData = modals.get(event.getSource());

      if(modalListData != null) {
        M modal = modalListData.getModal();
        modalListData.getCloseHandler().removeHandler();
        container.removeFromPopupSlot(modal);
        modals.remove(modal);
      }
    }
  }

  private class ModalListData<M> {
    private final M modal;

    private final HandlerRegistration closeHandler;

    public ModalListData(M modal, HandlerRegistration closeHandler) {
      this.modal = modal;
      this.closeHandler = closeHandler;
    }

    public M getModal() {
      return modal;
    }

    public HandlerRegistration getCloseHandler() {
      return closeHandler;
    }
  }

}
