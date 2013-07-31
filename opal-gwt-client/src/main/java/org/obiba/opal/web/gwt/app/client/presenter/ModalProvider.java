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
 *
 * Usage:
 *
 * @Inject
 * MyPresenter(ModalProvider<MyModal> modalProvider, ...) {
 *   this.modalProvider = modalProvider.setContainer(this);
 * }
 *
 * public void showModal() {
 *   MyModal modal = modalProvider.get();
 * }
 *
 * The get() method adds the modal to its container triggering a show() on the modal. Once the modal is closed
 * the provider ModalCloseHandler does the cleanup and removes it from its container slot triggering a hide() on the
 * modal.
 *
 */
public class ModalProvider<M extends ModalPresenterWidget> implements Provider<M>{

  private final Provider<M> modalProvider;
  private final EventBus eventBus;
  private PresenterWidget container;
  private HandlerRegistration handlerRegistration;

  private final Map<M, M> sources = new HashMap<M, M>();

  @Inject
  public ModalProvider(EventBus eventBus, Provider<M> modalProvider) {
    this.eventBus = eventBus;
    this.modalProvider = modalProvider;
  }

  public ModalProvider<M> setContainer(PresenterWidget container) {
    this.container = container;
    return this;
  }

  public M get() {

    if (container == null) {
      throw new NullPointerException("Modal container is not set. Call ModalProvider.setContainer(PresenterWidget)");
    }

    handlerRegistration = eventBus.addHandler(ModalClosedEvent.getType(), new ModalClosedHandler());
    M modal = modalProvider.get();
    container.addToPopupSlot(modal);
    sources.put(modal, modal);
    return modal;
  }

  private class ModalClosedHandler implements ModalClosedEvent.Handler {

    @Override
    public void onModalClosed(ModalClosedEvent event) {
      handlerRegistration.removeHandler();
      M modal = sources.get(event.getSource());

      if (modal != null) {
        container.removeFromPopupSlot(modal);
        sources.remove(modal);
      }
    }
  }
}
